---
name: project-execution-authorization
description: Enforce explicit authorization before running frontend, backend, Docker, or integrated-browser operations in this repository.
---

# Project Execution Authorization

Use this skill for any task in this repository that could run a project, invoke its
frontend or backend tooling, operate Docker, or open/use the integrated browser.

## Authorization boundary

Before each such operation, obtain explicit authorization from the user in the current
conversation. Prior approval for an implementation, diagnosis, test, build, deployment,
or a previous command does not authorize a later execution.

The following require explicit authorization:

- Frontend commands, including `npm`, `npx`, `ng`, package scripts, tests, builds,
  development servers, and commands run from `frontend/`.
- Backend commands, including Maven or Maven Wrapper goals, Java/JAR execution, tests,
  builds, development servers, and commands run from `backend/`.
- Any Docker or Docker Compose command, including status, logs, builds, starts, stops,
  pulls, execs, and configuration checks.
- Opening or interacting with the integrated browser, including browser tabs and UI
  automation.

Read-only filesystem inspection and editing source or documentation are allowed without
that authorization. Do not substitute an unapproved project command with a different
project command or browser action. State what verification was not run and wait for the
user's authorization.
