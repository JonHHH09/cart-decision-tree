package cart.decisiontree.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import cart.decisiontree.application.model.CartApplicationException;
import cart.decisiontree.application.port.in.DatasetSummaryUseCase;
import cart.decisiontree.application.port.in.DatasetSummaryUseCase.DatasetSummary;
import cart.decisiontree.application.port.in.EvaluateTreeUseCase;
import cart.decisiontree.application.port.in.EvaluateTreeUseCase.Evaluation;
import cart.decisiontree.application.port.in.PredictFruitUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase.Prediction;
import cart.decisiontree.application.port.in.RenderTreeUseCase;
import cart.decisiontree.application.port.in.RenderTreeUseCase.RenderedNode;
import cart.decisiontree.application.port.in.RenderTreeUseCase.RenderedTree;
import cart.decisiontree.application.port.in.TrainTreeUseCase;
import cart.decisiontree.application.port.in.TrainTreeUseCase.TrainingResult;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartWebController.class)
@Import(CartWebExceptionHandler.class)
class CartWebControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DatasetSummaryUseCase datasetSummary;

    @MockitoBean
    TrainTreeUseCase training;

    @MockitoBean
    PredictFruitUseCase prediction;

    @MockitoBean
    RenderTreeUseCase rendering;

    @MockitoBean
    EvaluateTreeUseCase evaluation;

    @Test
    void rendersTheCompleteDashboardWithoutAnActiveModel() throws Exception {
        when(datasetSummary.summarizeDataset()).thenReturn(summary());
        when(rendering.renderTree()).thenThrow(new CartApplicationException("MODEL_NOT_TRAINED", "Train first."));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CART Fruit Classifier")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No active model")));
    }

    @Test
    void returnsTrainingFragmentsForHtmxAndFullPagesWithoutJavascript() throws Exception {
        when(training.trainTree()).thenReturn(new TrainingResult("abc", 4, 3, 2));
        when(rendering.renderTree()).thenReturn(tree());
        when(evaluation.evaluateTree()).thenReturn(evaluation());
        when(datasetSummary.summarizeDataset()).thenReturn(summary());

        mockMvc.perform(post("/ui/tree/train").header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/training :: content"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Retrain decision tree")));

        mockMvc.perform(post("/ui/tree/train"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CART Fruit Classifier")));
    }

    @Test
    void returnsPredictionFragmentsAndEscapesFailures() throws Exception {
        when(prediction.predict(any())).thenReturn(new Prediction("apple", Map.of("apple", 2), Map.of("apple", "100%")));

        mockMvc.perform(post("/ui/predictions").header("HX-Request", "true")
                        .param("color", "red").param("shape", "round").param("weightGrams", "180").param("skin", "smooth"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/prediction :: content"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("apple")));

        when(prediction.predict(any())).thenThrow(new CartApplicationException("INVALID_INPUT", "<script>alert(1)</script>"));

        mockMvc.perform(post("/ui/predictions").header("HX-Request", "true")
                        .param("color", "red").param("shape", "round").param("weightGrams", "180").param("skin", "smooth"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")));
    }

    @Test
    void sanitizesDatabaseFailuresForHumanRequests() throws Exception {
        when(datasetSummary.summarizeDataset())
                .thenThrow(new DataAccessResourceFailureException("jdbc:postgresql://secret-host/private"));

        mockMvc.perform(get("/").header("HX-Request", "true"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DATASET_UNAVAILABLE")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-host"))));
    }

    private static DatasetSummary summary() {
        return new DatasetSummary(4, 2, List.of("apple", "banana"),
                Map.of("color", List.of("red"), "shape", List.of("round"), "skin", List.of("smooth")), 100, 200);
    }

    private static RenderedTree tree() {
        return new RenderedTree("abc", List.of(
                new RenderedNode(0, "root", "Is color == red?", Map.of()),
                new RenderedNode(1, "true", "", Map.of("apple", 2))), "tree");
    }

    private static Evaluation evaluation() {
        return new Evaluation(2, 2, 1.0, Map.of("apple", Map.of("apple", 2)));
    }
}
