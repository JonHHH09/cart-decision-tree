package cart.decisiontree.adapter.in.mcp;

import cart.decisiontree.application.model.CartApplicationException;
import cart.decisiontree.application.port.in.DatasetSummaryUseCase;
import cart.decisiontree.application.port.in.EvaluateTreeUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase.PredictFruitCommand;
import cart.decisiontree.application.port.in.RenderTreeUseCase;
import cart.decisiontree.application.port.in.TrainTreeUseCase;

import java.util.function.Supplier;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused") // Spring AI invokes @McpTool methods reflectively.
public final class CartMcpTools {

    private final DatasetSummaryUseCase datasetSummary;
    private final TrainTreeUseCase training;
    private final PredictFruitUseCase prediction;
    private final RenderTreeUseCase rendering;
    private final EvaluateTreeUseCase evaluation;

    public CartMcpTools(DatasetSummaryUseCase datasetSummary, TrainTreeUseCase training,
                        PredictFruitUseCase prediction, RenderTreeUseCase rendering, EvaluateTreeUseCase evaluation) {
        this.datasetSummary = datasetSummary;
        this.training = training;
        this.prediction = prediction;
        this.rendering = rendering;
        this.evaluation = evaluation;
    }

    @McpTool(name = "cart_dataset_summary", title = "CART dataset summary",
            description = "Summarize the seeded fruit training and test datasets without exposing database details.",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
                    idempotentHint = true, openWorldHint = false))
    @SuppressWarnings("UnusedReturnValue")
    public DatasetSummaryUseCase.DatasetSummary datasetSummary() {
        return safely(datasetSummary::summarizeDataset);
    }

    @McpTool(name = "cart_train_tree", title = "Train CART tree",
            description = "Train and atomically activate a deterministic CART fruit classifier from the seeded dataset.",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(idempotentHint = true, openWorldHint = false))
    public TrainTreeUseCase.TrainingResult trainTree() {
        return safely(training::trainTree);
    }

    @McpTool(name = "cart_predict_fruit", title = "Predict fruit",
            description = "Classify one fruit with the active CART model and return label counts and probabilities.",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
                    idempotentHint = true, openWorldHint = false))
    public PredictFruitUseCase.Prediction predictFruit(
            @McpToolParam(description = "Fruit color, 1 to 32 characters.") String color,
            @McpToolParam(description = "Fruit shape, 1 to 32 characters.") String shape,
            @McpToolParam(description = "Fruit weight in grams, from 1 through 10000.") int weightGrams,
            @McpToolParam(description = "Fruit skin texture, 1 to 32 characters.") String skin) {
        return safely(() -> prediction.predict(new PredictFruitCommand(color, shape, weightGrams, skin)));
    }

    @McpTool(name = "cart_render_tree", title = "Render CART tree",
            description = "Return the active decision tree as bounded structured nodes and a text representation.",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
                    idempotentHint = true, openWorldHint = false))
    public RenderTreeUseCase.RenderedTree renderTree() {
        return safely(rendering::renderTree);
    }

    @McpTool(name = "cart_evaluate_tree", title = "Evaluate CART tree",
            description = "Evaluate the active tree against the seeded holdout dataset and return accuracy and confusion counts.",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
                    idempotentHint = true, openWorldHint = false))
    public EvaluateTreeUseCase.Evaluation evaluateTree() {
        return safely(evaluation::evaluateTree);
    }

    private static <T> T safely(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (CartApplicationException exception) {
            throw new McpOperationException(exception.code() + ": " + exception.getMessage());
        } catch (RuntimeException exception) {
            throw new McpOperationException("INTERNAL_ERROR: The operation could not be completed.");
        }
    }

    private static final class McpOperationException extends RuntimeException {

        private McpOperationException(String message) {
            super(message);
        }
    }
}
