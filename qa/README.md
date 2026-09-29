# Quality assurance evidence

Project creator and developer: **PolarDredd**.

`results/` stores dated build/test and emulator summaries. Existing September 23 results are historical evidence and do not certify later source changes or the current deployed backend.

For each new verification pass, record the commit, commands, device/API level if relevant, results, and remaining unverified behavior. Preserve historical results and use a new dated file for a new pass.

From the repository root, `.\verify-core.ps1` builds debug/release variants, runs JVM tests and lint, then probes the live backend. `-Emulator` also runs device tests. Inspect the backend probe's printed status, not just process success. See [stabilization notes](../STABILIZATION.md) for live acceptance steps.
