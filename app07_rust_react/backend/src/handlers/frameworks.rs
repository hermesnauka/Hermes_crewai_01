use axum::{
    Json,
    extract::{Path, State},
};

use crate::{
    error::{AppError, AppResult},
    models::Framework,
    state::AppState,
};

pub async fn list_frameworks(State(state): State<AppState>) -> AppResult<Json<Vec<Framework>>> {
    let frameworks = sqlx::query_as!(
        Framework,
        r#"SELECT id, code, name, version, description, reference_url
           FROM framework
           ORDER BY code"#
    )
    .fetch_all(&state.pool)
    .await?;

    Ok(Json(frameworks))
}

pub async fn get_framework(
    State(state): State<AppState>,
    Path(code): Path<String>,
) -> AppResult<Json<Framework>> {
    let framework = sqlx::query_as!(
        Framework,
        r#"SELECT id, code, name, version, description, reference_url
           FROM framework
           WHERE UPPER(code) = UPPER($1)"#,
        code
    )
    .fetch_optional(&state.pool)
    .await?
    .ok_or_else(|| AppError::NotFound("Framework", code.clone()))?;

    Ok(Json(framework))
}
