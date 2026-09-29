# Application source map

Project creator and developer: **PolarDredd**. See [credits](../../../../../../../CREDITS.md).

`MainActivity.kt` hosts navigation and session-aware routing. The package path mirrors the application namespace.

| Folder | Responsibility and maintenance notes |
| --- | --- |
| `data/` | [Authentication, persistence, matching, search, and safe diagnostics](data/README.md). |
| `model/` | [Shared application data types](model/README.md). |
| `network/` | [Legacy PHP API contracts and Retrofit client](network/README.md). |
| `ui/` | [Fragments, ViewModels, adapters, and screen-specific behavior](ui/README.md). |

Typical flow: a fragment observes its ViewModel, the ViewModel reads a repository, and repository data is mapped into application models. Some fragments call repositories directly; inspect the affected screen rather than assuming every feature has a ViewModel.

Published job screens use Supabase through `PublishedJobsRepository`. Profile and resume data feed local matching. Coordinate model/schema changes with the admin integration and database guides.
