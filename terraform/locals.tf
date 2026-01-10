locals {
  base_name = "${var.project_name}-${var.environment}-${var.instance}"

  rg_name     = local.base_name
  logs_name   = "${local.base_name}-logs"
  env_name    = "${local.base_name}-env"
  app_name    = local.base_name
  kv_name     = "kv-${local.base_name}"
  pg_name     = "${local.base_name}-pg"
  acr_name    = "${replace(local.base_name, "-", "")}acr"
}
