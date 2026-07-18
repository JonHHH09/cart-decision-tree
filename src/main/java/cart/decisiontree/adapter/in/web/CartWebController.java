package cart.decisiontree.adapter.in.web;

import cart.decisiontree.application.model.CartApplicationException;
import cart.decisiontree.application.port.in.DatasetSummaryUseCase;
import cart.decisiontree.application.port.in.EvaluateTreeUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase.PredictFruitCommand;
import cart.decisiontree.application.port.in.RenderTreeUseCase;
import cart.decisiontree.application.port.in.TrainTreeUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@SuppressWarnings({"SpringMVCViewInspection", "HttpHeaderInspection"})
public final class CartWebController {

    private final DatasetSummaryUseCase datasetSummary;
    private final TrainTreeUseCase training;
    private final PredictFruitUseCase prediction;
    private final RenderTreeUseCase rendering;
    private final EvaluateTreeUseCase evaluation;

    public CartWebController(DatasetSummaryUseCase datasetSummary, TrainTreeUseCase training,
                             PredictFruitUseCase prediction, RenderTreeUseCase rendering, EvaluateTreeUseCase evaluation) {
        this.datasetSummary = datasetSummary;
        this.training = training;
        this.prediction = prediction;
        this.rendering = rendering;
        this.evaluation = evaluation;
    }

    @GetMapping("/")
    String dashboard(Model model) {
        populateDashboard(model);
        return "dashboard";
    }

    @PostMapping("/ui/tree/train")
    String train(@RequestHeader(name = "HX-Request", required = false) String hxRequest, Model model) {
        try {
            model.addAttribute("training", training.trainTree());
            addTreeAndEvaluation(model);
        } catch (CartApplicationException exception) {
            addError(model, exception);
        }
        if (isHtmx(hxRequest)) {
            return "fragments/training :: content";
        }
        populateSummary(model);
        return "dashboard";
    }

    @PostMapping("/ui/predictions")
    String predict(@RequestParam String color, @RequestParam String shape, @RequestParam int weightGrams,
                   @RequestParam String skin, @RequestHeader(name = "HX-Request", required = false) String hxRequest,
                   Model model) {
        try {
            model.addAttribute("prediction", prediction.predict(new PredictFruitCommand(color, shape, weightGrams, skin)));
        } catch (CartApplicationException exception) {
            addError(model, exception);
        }
        if (isHtmx(hxRequest)) {
            return "fragments/prediction :: content";
        }
        populateDashboard(model);
        return "dashboard";
    }

    @GetMapping("/ui/tree")
    String tree(@RequestHeader(name = "HX-Request", required = false) String hxRequest, Model model) {
        try {
            model.addAttribute("tree", rendering.renderTree());
        } catch (CartApplicationException exception) {
            addError(model, exception);
        }
        if (isHtmx(hxRequest)) {
            return "fragments/tree :: content";
        }
        populateDashboard(model);
        return "dashboard";
    }

    @GetMapping("/ui/evaluation")
    String evaluation(@RequestHeader(name = "HX-Request", required = false) String hxRequest, Model model) {
        try {
            model.addAttribute("evaluation", evaluation.evaluateTree());
        } catch (CartApplicationException exception) {
            addError(model, exception);
        }
        if (isHtmx(hxRequest)) {
            return "fragments/evaluation :: content";
        }
        populateDashboard(model);
        return "dashboard";
    }

    private void populateDashboard(Model model) {
        populateSummary(model);
        addTreeAndEvaluation(model);
    }

    private void populateSummary(Model model) {
        try {
            model.addAttribute("dataset", datasetSummary.summarizeDataset());
        } catch (CartApplicationException exception) {
            addError(model, exception);
        }
    }

    private void addTreeAndEvaluation(Model model) {
        try {
            model.addAttribute("tree", rendering.renderTree());
            model.addAttribute("evaluation", evaluation.evaluateTree());
        } catch (CartApplicationException exception) {
            if (!"MODEL_NOT_TRAINED".equals(exception.code())) {
                addError(model, exception);
            }
        }
    }

    private static void addError(Model model, CartApplicationException exception) {
        model.addAttribute("errorCode", exception.code());
        model.addAttribute("errorMessage", exception.getMessage());
    }

    private static boolean isHtmx(String header) {
        return "true".equalsIgnoreCase(header);
    }
}
