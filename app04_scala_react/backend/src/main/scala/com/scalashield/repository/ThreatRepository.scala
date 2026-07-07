package com.scalashield.repository

import com.scalashield.db.Ctx
import zio._

import java.sql.SQLException
import java.util.UUID
import javax.sql.DataSource

/** DB row shape - separate from the API-facing model.ThreatSummary/
  * ThreatDetail because those expose richer types (Set[StrideCategory],
  * frameworkCode instead of frameworkId) that don't map 1:1 onto columns.
  * `cve_references` is deliberately not read here - no Phase 1 seed row
  * ever populates it, so the API layer just returns List.empty for it
  * rather than threading an always-empty column through Quill.
  */
final case class ThreatRow(
    id: UUID,
    frameworkId: UUID,
    code: String,
    title: String,
    severity: String,
    category: String,
    description: String,
    attackVector: String,
    attackSurface: String,
    stride: String,
    tags: String,
)

final case class ThreatFilter(
    frameworkId: Option[UUID] = None,
    severity: Option[String] = None,
    stride: Option[String] = None,
    category: Option[String] = None,
    tag: Option[String] = None,
    q: Option[String] = None,
)

object ThreatRepository:
  import Ctx._

  private inline def threatSchema = querySchema[ThreatRow]("threat")

  /** D-02: ZIO Quill dynamic query. Filters are only known at request time,
    * so this uses Quill's dynamicQuerySchema API rather than a fully static
    * `quote { ... }` block - but every predicate still goes through the
    * same compile-time-checked `quote`/`lift` machinery per filter, so
    * string-concatenated SQL is still not possible here.
    */
  def search(filter: ThreatFilter): ZIO[DataSource, SQLException, List[ThreatRow]] =
    val base = dynamicQuerySchema[ThreatRow]("threat")
    val filtered = base
      .filterOpt(filter.frameworkId)((row, v) => quote(row.frameworkId == lift(v)))
      .filterOpt(filter.severity)((row, v) => quote(row.severity == lift(v.toUpperCase)))
      .filterOpt(filter.stride)((row, v) => quote(row.stride like lift(s"%${v.toUpperCase}%")))
      .filterOpt(filter.category)((row, v) => quote(row.category == lift(v)))
      .filterOpt(filter.tag)((row, v) => quote(row.tags like lift(s"%$v%")))
      .filterOpt(filter.q)((row, v) =>
        quote(row.title.toLowerCase.like(lift(s"%${v.toLowerCase}%")) || row.description.toLowerCase.like(lift(s"%${v.toLowerCase}%")))
      )
    Ctx.run(filtered)

  def findById(id: UUID): ZIO[DataSource, SQLException, Option[ThreatRow]] =
    Ctx.run(threatSchema.filter(t => t.id == lift(id))).map(_.headOption)
