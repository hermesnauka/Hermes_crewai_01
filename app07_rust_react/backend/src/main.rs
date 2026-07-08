mod config;
mod error;

use axum::{Json, Router, routing::get};
use config::Config;
use serde_json::json;
use sqlx::postgres::PgPoolOptions;

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt()
        .with_env_filter(tracing_subscriber::EnvFilter::try_from_default_env().unwrap_or_else(
            |_| tracing_subscriber::EnvFilter::new("info,securevision_backend=debug"),
        ))
        .init();

    let config = Config::from_env();

    let pool = PgPoolOptions::new()
        .max_connections(10)
        .connect(&config.database_url)
        .await
        .expect("failed to connect to database");

    sqlx::migrate!("./migrations")
        .run(&pool)
        .await
        .expect("failed to run migrations");

    let app = Router::new().route("/health", get(health));

    let listener = tokio::net::TcpListener::bind(&config.bind_addr)
        .await
        .expect("failed to bind");
    tracing::info!("listening on {}", config.bind_addr);
    axum::serve(listener, app).await.expect("server error");
}

async fn health() -> Json<serde_json::Value> {
    Json(json!({ "status": "UP" }))
}
