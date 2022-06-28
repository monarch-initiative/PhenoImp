# How to release

Follow this checklist when performing a *PhenoImp* release.

1. Pre-release updates:
   1. Update the hard-coded version in `phenoimp-cli/*/Main.java`
   2. Update `CHANGELOG.rst`
2. Perform the release, including tagging the commit, building the distribution, and uploading the distribution archive to GitHub releases page.
3. Build a Docker container.
4. Start next development iteration, including updating the hard-coded version in `phenoimp-cli/*/Main.java`.
