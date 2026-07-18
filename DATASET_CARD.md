# Dataset Card — Demonstration Fruit Data

## Contents

Flyway migration `V1__create_fruit_dataset_schemas.sql` creates:

- `training_data.fruit_examples`: 24 rows.
- `test_data.fruit_examples`: 11 holdout rows.

Each row contains `color`, `shape`, `weight_grams`, `skin`, and `fruit`. Database identifiers provide stable loading order but are not model features.

## Purpose

The dataset exists solely to demonstrate CART behavior and application workflows. It is not a scientific benchmark, production corpus, or statistically representative sample.

## Provenance and license

The dataset is stored directly in the repository migration. On 2026-07-18, maintainer Joni Hysaj confirmed authorship of all 35 rows and licensed them under the repository's MIT license. Before accepting rows copied from an external source, contributors must document the source, collection method, usage rights, and required attribution here.

## Privacy and ethics

The rows describe fruit characteristics and contain no personal data. The dataset must not be extended with personal, confidential, regulated, or proprietary information.

## Reproducibility and quality

- Migration-controlled rows make clean database creation reproducible.
- Training and holdout schemas are separate.
- Application code reads rows in stable identifier order and fingerprints normalized training content.
- Runtime boundaries reject oversized materialized datasets.

## Limitations

- Very small sample size and narrow feature vocabulary.
- Manually curated demonstration values; no documented sampling process.
- No class-balance guarantee, measurement-error study, deduplication report, or external validation.
- Holdout results must not be generalized beyond these fixed rows.
