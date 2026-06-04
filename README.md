# Supermarket Checkout System

A supermarket checkout system in **Java**, built for the CentraleSupélec course
*2EL1520 — Génie logiciel orienté objet* (2025–2026). It computes bills (discount
plans, category pricing, delivery), processes card payments through a POS/bank
model, and manages a live inventory with low-stock alerts. It is driven by a
command-line interface (CLUI).

## Two runnable versions

This repository provides **two versions** the evaluator can run:

| Version | What it is | Where |
|---------|-----------|-------|
| **Specification version** | The project exactly as required by the instructions (R1–R10, the CLUI, the mandatory tests). No extra features, no JavaFX. | Tag **`v1.0`** — GitHub *Release* **v1.0** (downloadable `.zip`) |
| **Final version** | The specification version **plus** the extensions (undo/redo, promotions, VAT, loyalty points and a JavaFX GUI). | Branch **`master`** — latest GitHub *Release* |

> Download the corresponding source `.zip` from the repository's **Releases**
> page, or, with git: `git checkout v1.0` for the specification version and
> `git checkout master` for the final version.
>
> The extensions are additive and default to no-op, so the **final version
> reproduces the exact specified behaviour** as well; the `v1.0` version is
> provided so the specified system can be run in isolation.

## Build & run

Requires **JDK 17+** (developed on JDK 21) and **Maven 3.9+**.

```bash
mvn test            # compile + run all JUnit tests
mvn exec:java -Dexec.mainClass="supermarket.cli.CLI"   # launch the CLUI
mvn javafx:run      # launch the JavaFX desktop GUI   (final version only)
```

Inside the CLUI, run a scenario with:

```
runTest testScenario1.txt
```

At startup the CLUI auto-loads `my_supermarket.ini` (the standard setup). Type
`help` to list every command, `exit` to quit. The final version provides four
scenario files (`testScenario1..4.txt`); the specification version provides the
mandatory ones.

## Design patterns

Strategy (discount plans, pricing policies, delivery), Observer (low-stock
alerts), Factory (discount plans), Facade (bank authorisation) — plus Command
(undo/redo) and an additional Strategy (promotions) in the final version.

## Project structure

```
src/main/java/supermarket/
  model, users          domain entities and user roles
  discount, pricing      Strategy + Factory for plans and category pricing
  inventory              Observer-based stock alerts
  payment                POS + TAS (Facade)
  delivery               delivery charging (Strategy) + slot logistics
  register               Supermarket (core) + CashRegister (checkout)
  cli                    command-line UI
  promotion, command, loyalty, gui   extensions (final version)
src/test/java/supermarket/   one test class per unit
docs/                   report (LaTeX) and UML diagrams
testScenario*.txt        end-to-end CLUI scenarios
```

## Documentation

- **Report (PDF):** [`docs/report/Report.pdf`](docs/report/Report.pdf) — the full written report.
- **Report sources:** `docs/report/` (LaTeX; build `main.tex` with `pdflatex`).
- **UML diagrams:** `docs/uml/` (PlantUML sources + rendered PNGs).
