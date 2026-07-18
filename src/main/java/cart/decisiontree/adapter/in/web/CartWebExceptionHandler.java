package cart.decisiontree.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public final class CartWebExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    ModelAndView databaseUnavailable(HttpServletRequest request) {
        var view = "true".equalsIgnoreCase(request.getHeader("HX-Request")) ? "fragments/error :: content" : "error";
        var modelAndView = new ModelAndView(view, HttpStatus.SERVICE_UNAVAILABLE);
        modelAndView.addObject("errorCode", "DATASET_UNAVAILABLE");
        modelAndView.addObject("errorMessage", "The fruit dataset is temporarily unavailable.");
        return modelAndView;
    }
}
