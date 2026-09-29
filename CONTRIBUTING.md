# Developer handoff and commit comments

Project creator and developer: **PolarDredd**. Preserve [project credits](CREDITS.md) and attribute new contributions to their actual authors.

## Before changing a folder

Read the root README and the nearest folder README. Update that guide when a folder's responsibilities, setup, data flow, or verification steps change. For a new maintained folder, add a short README explaining its purpose, important files, dependencies, maintenance cautions, and project credit. Cover resource subfolders in the resource guide rather than placing Markdown inside Android resource directories.

Use code comments to explain a non-obvious constraint or decision near the affected code. Folder guides explain the broader structure. Generated caches, generated source, vendored tools, and binary assets do not need repetitive author comments.

## Every commit

Write a concrete subject and a body explaining:

1. What changed and why.
2. Each affected folder and what the next developer needs to know.
3. Checks actually performed, their result, and any remaining limitations.
4. Project credit: PolarDredd. Keep the commit author as the person making the change.

A template is supplied in [`.gitmessage`](.gitmessage). Enable it for a new clone:

```powershell
git config --local commit.template .gitmessage
git commit
```

Git opens the template for editor-based commits. Replace its placeholders before saving. Commands using `git commit -m` and some GUI clients bypass the template; include the same information manually. This template guides commit writing and does not automatically enforce it.

Example:

```text
docs(app): explain Android folder responsibilities

Why:
Make the source layout understandable to the next developer.

Folders:
- app/: document build commands and generated APK location.
- app/src/main/java/com/mrdiy/careers/: map screens to data repositories.

Validation:
Checked documented paths and reviewed the diff. Runtime tests not run:
documentation only.

Project credit: PolarDredd
```

Stage specific files and inspect `git diff --cached` before committing. Preserve existing author records. Rewrite historical commits only as a separate, explicitly agreed operation, because doing so changes commit IDs and can disrupt shared branches.
