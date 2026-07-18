package cart.decisiontree.domain.model;

public record DecisionNode(Question question, TreeNode trueBranch, TreeNode falseBranch) implements TreeNode {
}
