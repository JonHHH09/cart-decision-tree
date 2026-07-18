package cart.decisiontree.domain.model;

public sealed interface TreeNode permits Leaf, DecisionNode {
}
