package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.List;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * @author 王宁 version 创建时间：2018年9月27日 上午9:22:02 处理树
 */
public class TreeProcessor
{
	/**
	 * 将Tree<String>>转化为Tree<Annotation>
	 * 
	 * @param trees
	 * @param convertTable
	 * @return 获得树库的Tree<Annotation>表示
	 */
	public static List<Tree<Annotation>> getAnnotationTrees(List<Tree<String>> trees, NonterminalTable convertTable)
	{
		if (trees.isEmpty() || trees == null)
			return null;
		List<Tree<Annotation>> annotationTrees = new ArrayList<Tree<Annotation>>(trees.size());

		for (Tree<String> tree : trees)
		{
			Tree<Annotation> annotationtree = getAnnotationTreesHelper(tree, convertTable);
			annotationTrees.add(annotationtree);
		}
		return annotationTrees;
	}

	public static Tree<Annotation> getAnnotationTreesHelper(Tree<String> tree, NonterminalTable convertTable)
	{
		if (tree == null)
			return null;
		if (tree.isLeaf())
		{
			return new Tree<Annotation>(new Annotation(tree.getLabel()));
		}
		short intValueOfLabel;
		if (convertTable.hasSymbol(tree.getLabel()))
		{
			intValueOfLabel = convertTable.intValue(tree.getLabel());
		}
		else
		{
			intValueOfLabel = convertTable.putSymbol(tree.getLabel());
		}

		if (tree.isPreterminal() && !convertTable.getIntValueOfPreterminalArr().contains(intValueOfLabel))
		{
			convertTable.addToPreterminalArr(intValueOfLabel);
		}

		Tree<Annotation> annotationTree = new Tree<Annotation>(new Annotation(intValueOfLabel, (short) 1));
		ArrayList<Tree<Annotation>> children = new ArrayList<Tree<Annotation>>(tree.getChildren().size());
		for (Tree<String> child : tree.getChildren())
		{
			children.add(getAnnotationTreesHelper(child, convertTable));
		}
		annotationTree.setChildren(children);
		return annotationTree;
	}

	/**
	 * 将TreeNode转化为Tree<String>
	 * 
	 * @param tree
	 * @return Tree<String>
	 */
	public static Tree<String> getNormalTree(TreeNode treeNode)
	{
		if (treeNode == null)
			return null;
		if (treeNode.isLeaf())
			return new Tree<String>(treeNode.getNodeName());
		Tree<String> tree = new Tree<String>(treeNode.getNodeName());
		ArrayList<Tree<String>> children = new ArrayList<Tree<String>>(treeNode.getChildren().size());
		for (TreeNode child : treeNode.getChildren())
		{
			children.add(getNormalTree(child));
		}
		tree.setChildren(children);
		return tree;
	}

	public static Tree<String> binarizeTree(Tree<String> normalTree)
	{
		List<Tree<String>> children = normalTree.getChildren();
		if ((children.size() == 1 && children.get(0).getChildren().isEmpty()) || children.isEmpty())
			return normalTree;
		List<Tree<String>> newChildren = new ArrayList<Tree<String>>(children.size());
		for (Tree<String> child : children)
		{
			newChildren.add(binarizeTree(child));
		}

		children = newChildren;
		if (children.size() == 1)
		{
			children = new ArrayList<Tree<String>>(1);
			children = newChildren;
		}
		if (children.size() >= 3)
		{
			// children转化为left、right child
			Tree<String> leftChild = new Tree<String>("@" + normalTree.getLabel());
			Tree<String> rightChild = children.get(children.size() - 1);
			List<Tree<String>> tempChildrenOfLC = new ArrayList<Tree<String>>(2);
			tempChildrenOfLC.add(children.get(0));
			tempChildrenOfLC.add(children.get(1));
			for (int i = 2; i < children.size() - 1; i++)
			{
				Tree<String> tempTree = new Tree<String>("@" + normalTree.getLabel());
				tempTree.setChildren(tempChildrenOfLC);
				tempChildrenOfLC = new ArrayList<Tree<String>>(2);
				tempChildrenOfLC.add(tempTree);
				tempChildrenOfLC.add(children.get(i));

			}
			leftChild.setChildren(tempChildrenOfLC);
			children = new ArrayList<Tree<String>>(2);
			children.add(leftChild);
			children.add(rightChild);
		}

		normalTree.setChildren(children);

		return normalTree;
	}

	private static void recoverBinaryTreeHelper(Tree<String> binaryTree, List<Tree<String>> realChildren)
	{
		if (binaryTree.getChildren().get(0).getLabel().startsWith("@"))
		{
			recoverBinaryTreeHelper(binaryTree.getChildren().get(0), realChildren);
			realChildren.add(binaryTree.getChildren().get(1));
		}
		else
		{
			realChildren.add(binaryTree.getChildren().get(0));
			realChildren.add(binaryTree.getChildren().get(1));
		}
	}

	public static void recoverBinaryTree(Tree<String> binaryTree)
	{
		if (binaryTree.getChildren().isEmpty()
				|| (binaryTree.getChildren().size() == 1 && binaryTree.getChildren().get(0).getChildren().isEmpty()))
			return;
		if (binaryTree.getChildren().get(0).getLabel().startsWith("@"))
		{
			List<Tree<String>> tempChildren = new ArrayList<Tree<String>>();
			recoverBinaryTreeHelper(binaryTree, tempChildren);
			List<Tree<String>> realChildren = new ArrayList<Tree<String>>(tempChildren.size());
			realChildren.addAll(tempChildren);
			binaryTree.setChildren(realChildren);
		}
		for (int i = 0; i < binaryTree.getChildren().size(); i++)
		{
			recoverBinaryTree(binaryTree.getChildren().get(i));
		}
	}

	/**
	 * 
	 * @param tree
	 * @return 移除树中例如A->A的结构
	 */
	public static Tree<String> removeL2LRule(Tree<String> tree)
	{
		if (tree.getChildren().isEmpty()
				|| (tree.getChildren().size() == 1 && tree.getChildren().get(0).getChildren().isEmpty()))
			return tree;
		for (int i = 0; i < tree.getChildren().size(); i++)
		{
			removeL2LRule(tree.getChildren().get(i));
		}
		if (tree.getChildren().size() == 1 && !tree.getChildren().get(0).getChildren().isEmpty()
				&& tree.getLabel().equals(tree.getChildren().get(0).getLabel()))
		{
			tree.setChildren(tree.getChildren().get(0).getChildren());
		}
		return tree;
	}

}
