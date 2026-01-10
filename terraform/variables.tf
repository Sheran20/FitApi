variable "project_name" {
  description = "Logical project name (e.g. fitapi)"
  type        = string
}

variable "environment" {
  description = "Environment name (dev, prod)"
  type        = string
}

variable "instance" {
  description = "Instance suffix for parallel deployments (e.g. 1, tf, test)"
  type        = string
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "canadacentral"
}

variable "postgres_admin" {
  description = "PostgreSQL admin username"
  type        = string
}

variable "postgres_password" {
  description = "PostgreSQL admin password"
  type        = string
  sensitive   = true
}
