# Product roadmap

The foundation is implemented: a data-backed UI, household-scoped API, equal expense splits, testing, container setup, and CI/CD. The items below are open GitHub stories. Each contains a user story, benefit/value, story points, acceptance criteria, dependencies, and tests.

Proposed cadence: three two-week sprints after kickoff. Dates are intentionally unassigned until capacity and priority are agreed. Story points are relative estimates, not hours or delivery promises.

| Sprint | Goal | Stories | Points |
| --- | --- | --- | --- |
| 1 | Prepare data and access controls for public use | #1, #2 | 13 |
| 2 | Expand expense and reimbursement workflows | #3, #4, #5 | 21 |
| 3 | Improve daily use and prepare a hosted demo | #6, #7, #8, #9 | 16 |

## Backlog

| Story | Points | Priority | Proposed sprint |
| --- | --- | --- | --- |
| [#1 — Harden household membership and authentication for public use](https://github.com/KhushiShah-swe/NestSync/issues/1) | 8 | P0 | 1 |
| [#2 — Introduce versioned database migrations and recovery procedures](https://github.com/KhushiShah-swe/NestSync/issues/2) | 5 | P0 | 1 |
| [#3 — Support percentage and custom expense splits](https://github.com/KhushiShah-swe/NestSync/issues/3) | 8 | P1 | 2 |
| [#4 — Record settlements and generate a suggested repayment plan](https://github.com/KhushiShah-swe/NestSync/issues/4) | 8 | P1 | 2 |
| [#5 — Attach and manage expense receipts](https://github.com/KhushiShah-swe/NestSync/issues/5) | 5 | P2 | 2 |
| [#6 — Generate recurring chores and household reminders](https://github.com/KhushiShah-swe/NestSync/issues/6) | 5 | P1 | 3 |
| [#7 — Export household expenses and balances](https://github.com/KhushiShah-swe/NestSync/issues/7) | 3 | P2 | 3 |
| [#8 — Audit accessibility and expand mobile browser coverage](https://github.com/KhushiShah-swe/NestSync/issues/8) | 3 | P2 | 3 |
| [#9 — Deploy a public portfolio demo with monitoring and rollback](https://github.com/KhushiShah-swe/NestSync/issues/9) | 5 | P1 | 3 |

## Working agreement

Use Backlog → Ready → In progress → In review → Done as board states. These states and sprint groupings are a planning guide; a GitHub Projects board and native milestones have not been configured. The repository includes issue forms and a pull request template to support this workflow.

Before a sprint, refine dependencies and acceptance criteria. During the sprint, keep work in progress small and link changes to issues. Close a story only when its acceptance criteria, tests, documentation, and review are complete. Run a short demonstration and retrospective after each sprint.

## Future discovery

Shared calendar, chat, multi-currency support, native mobile clients, and external payment integrations remain ideas. Select an actual user problem and define a scoped story before starting them. A source-code license also remains the project owner's decision.
