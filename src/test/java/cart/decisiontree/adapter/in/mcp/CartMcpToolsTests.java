package cart.decisiontree.adapter.in.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cart.decisiontree.application.model.CartApplicationException;
import cart.decisiontree.application.port.in.DatasetSummaryUseCase;
import cart.decisiontree.application.port.in.EvaluateTreeUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase.Prediction;
import cart.decisiontree.application.port.in.RenderTreeUseCase;
import cart.decisiontree.application.port.in.TrainTreeUseCase;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

class CartMcpToolsTests {

    @Test
    void exposesStableStructuredToolContractsAndDelegatesPrediction() {
        var datasetSummary = mock(DatasetSummaryUseCase.class);
        var training = mock(TrainTreeUseCase.class);
        var prediction = mock(PredictFruitUseCase.class);
        var rendering = mock(RenderTreeUseCase.class);
        var evaluation = mock(EvaluateTreeUseCase.class);
        var tools = new CartMcpTools(datasetSummary, training, prediction, rendering, evaluation);
        var expected = new Prediction("apple", Map.of("apple", 2), Map.of("apple", "100%"));
        when(prediction.predict(any())).thenReturn(expected);

        var names = Arrays.stream(CartMcpTools.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(McpTool.class))
                .map(method -> method.getAnnotation(McpTool.class).name())
                .collect(Collectors.toSet());

        assertThat(names).isEqualTo(Set.of("cart_dataset_summary", "cart_train_tree", "cart_predict_fruit",
                "cart_render_tree", "cart_evaluate_tree"));
        Arrays.stream(CartMcpTools.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(McpTool.class))
                .map(method -> method.getAnnotation(McpTool.class))
                .forEach(tool -> {
                    assertThat(tool.generateOutputSchema()).isTrue();
                    assertThat(tool.annotations().openWorldHint()).isFalse();
                });
        var predictionMethod = Arrays.stream(CartMcpTools.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("predictFruit"))
                .findFirst().orElseThrow();
        assertThat(predictionMethod.getParameters()).allMatch(parameter -> parameter.isAnnotationPresent(McpToolParam.class));
        assertThat(predictionMethod.getParameters()).extracting(java.lang.reflect.Parameter::getName)
                .containsExactly("color", "shape", "weightGrams", "skin");

        assertThat(tools.predictFruit("red", "round", 180, "smooth")).isSameAs(expected);
        verify(prediction).predict(new PredictFruitUseCase.PredictFruitCommand("red", "round", 180, "smooth"));
    }

    @Test
    void convertsKnownAndUnexpectedFailuresToSanitizedMessages() {
        var datasetSummary = mock(DatasetSummaryUseCase.class);
        var tools = new CartMcpTools(datasetSummary, mock(TrainTreeUseCase.class), mock(PredictFruitUseCase.class),
                mock(RenderTreeUseCase.class), mock(EvaluateTreeUseCase.class));
        when(datasetSummary.summarizeDataset()).thenThrow(new CartApplicationException("DATASET_EMPTY", "No training rows."));

        assertThatThrownBy(tools::datasetSummary).hasMessage("DATASET_EMPTY: No training rows.");

        doThrow(new IllegalStateException("jdbc:secret-host")).when(datasetSummary).summarizeDataset();

        assertThatThrownBy(tools::datasetSummary)
                .hasMessage("INTERNAL_ERROR: The operation could not be completed.")
                .hasMessageNotContaining("secret-host");
    }
}
