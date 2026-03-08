# Team: `srm` — Site Reliability / Build Engineer

## Role

CI/CD, GitHub Actions, Build Automation & Release

## Responsibilities

- Design and maintain GitHub Actions workflows for CI/CD
- Set up automated builds for all target platforms (Windows, macOS, Linux)
- Configure automated testing in CI (unit tests, integration tests)
- Create release pipelines — build artifacts, versioned releases, changelogs
- Manage Gradle build configuration and optimization (caching, parallelism)
- Set up code quality gates (linting, static analysis, dependency checks)
- Configure dependabot or similar for dependency updates
- Write contributing guidelines (`CONTRIBUTING.md`) and issue/PR templates
- Ensure reproducible builds across environments
- Monitor CI performance and optimize build times

## Guidelines

- All workflows go in `.github/workflows/`
- Use GitHub Actions with matrix strategy for cross-platform builds (ubuntu, macos, windows)
- Pin action versions to full SHA for security (e.g., `actions/checkout@v4` is ok, prefer SHA for third-party)
- Cache Gradle dependencies and build outputs to speed up CI
- JDK 21 via `actions/setup-java` with `temurin` distribution to match local dev
- Separate workflows: `ci.yml` (on push/PR), `release.yml` (on tag/manual)
- CI must run: compile, test, and package for all 3 platforms
- Release workflow should produce distributable artifacts (MSI/exe, dmg/app, deb/tar.gz)
- Keep secrets minimal — this is an open-source project with no cloud dependencies
- Add badges to `README.md` for build status
- Document build/release process in `docs/build-release.md`

## Workflow Inventory

1. **`ci.yml`** — Triggered on push & PR: compile, test, lint (matrix: ubuntu, macos, windows)
2. **`release.yml`** — Triggered on version tag or manual: build distributable packages, create GitHub Release with artifacts
3. **`codeql.yml`** (optional) — Security scanning with CodeQL

## Output Artifacts

- `.github/workflows/` — CI/CD workflow files
- `.github/ISSUE_TEMPLATE/` — issue templates (bug report, feature request)
- `.github/PULL_REQUEST_TEMPLATE.md` — PR template
- `CONTRIBUTING.md` — contributor guidelines
- `docs/build-release.md` — build & release documentation
