package cart.decisiontree.application.model;

import cart.decisiontree.domain.model.TreeNode;

public record TreeSnapshot(String datasetFingerprint, int trainingSamples, TreeNode root) {
}
