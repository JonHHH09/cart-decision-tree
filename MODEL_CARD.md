# Model Card — CART Fruit Classifier

## Summary

The application trains an in-memory classification and regression tree (CART) over the repository's small fruit dataset. It is an educational software-engineering example, not a general-purpose ML platform or production decision system.

## Intended use

- Demonstrate bounded CART training, prediction, rendering, and holdout evaluation.
- Demonstrate equivalent human-facing MVC/HTMX and agent-facing MCP workflows.
- Exercise deterministic application boundaries, PostgreSQL migrations, and release controls.

Do not use predictions for safety-critical, financial, medical, employment, legal, or other consequential decisions.

## Inputs and outputs

Inputs are color, shape, weight in grams, and skin description. The output is a fruit label with class probabilities derived from the trained leaf. Text and numeric inputs, dataset materialization, tree size, depth, and rendered output are bounded in code.

## Training and identity

Training reads rows in stable database identifier order. `CartApplicationService` computes a deterministic SHA-256 fingerprint over normalized training rows and stores that fingerprint with the in-memory tree snapshot. The classifier has no random seed because its current split selection is deterministic.

## Evaluation

The application reports accuracy and a confusion matrix against the fixed holdout schema. These values are demonstration metrics, not evidence of external validity: the dataset is tiny, synthetic/demo-oriented, and not representative of real-world fruit distributions.

## Limitations

- No persisted model artifact, immutable model manifest, registry alias, rollback, or cross-process model identity.
- No model-generation CAS protocol, drift monitoring, calibration analysis, fairness analysis, or production telemetry.
- No versioned prediction API or compatibility guarantee beyond the documented release policy.
- Predictions are valid only for the limited feature vocabulary represented by the demonstration dataset.

The repository therefore does **not** claim implementation of the broader JOB-85 MLOps architecture. Releases package and attest the Java application; they do not constitute a full model-registry or continuous-training system.
