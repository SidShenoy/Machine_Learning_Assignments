import java.io.*; 
import java.util.*;

/* Most variable, functions and class names in this code are self-explanatory, where they aren't we have added comments */ 

/*Each node in the decision tree is of this node type*/
class DecisionNode{
	int attrNumber;		//attribute value being tested at this node
	ArrayList<DecisionNode> children = new ArrayList<DecisionNode>();	//children of this node
	int most_probable_target_value_if_pruned;
}

/* This class deals with handling continuous valed attributes */
class ContAttr{
	int value;					//continuous attribute's value for current example
	String targetAttrValue;
}

class SortByValue implements Comparator<ContAttr>
{
    // Used for sorting in ascending order of
    // the 'value' int variable
    public int compare(ContAttr a, ContAttr b)
    {
        return a.value - b.value;
    }
}

public class DecisionTree {
	
	static ArrayList<String[]> attributeValues = new ArrayList<String[]>();
	DecisionNode root;
	static ArrayList<String[]> dataset = new ArrayList<String[]>(); 	//to store the entire dataset of training examples
	static ArrayList<String[]> validationset = new ArrayList<String[]>();	//to store the entire validationset of testing examples
	float next_best_accuracy;
	DecisionNode best_accuracy_node;
	
	DecisionTree()
	{
		root = new DecisionNode();
	}
	
	void compute_best_accuracy_among_all_nodes(DecisionNode currentNode)	
	{
		// testing the accuracy if the currentNode is pruned
		int attrNumber_of_current_node = currentNode.attrNumber;
		
		currentNode.attrNumber = currentNode.most_probable_target_value_if_pruned; //making the current node a leaf partially to check new accuracy
		
		float accuracy_if_current_node_is_pruned = compute_accuracy(root, 14);
		
		if(next_best_accuracy < accuracy_if_current_node_is_pruned)
		{
			next_best_accuracy = accuracy_if_current_node_is_pruned;
			best_accuracy_node = currentNode;
		}
		
		currentNode.attrNumber = attrNumber_of_current_node;
		
		//testing the accuracy if children of the currentNode are pruned one by one
		if(currentNode.attrNumber < 14)
		{
			for(int i=0;i<currentNode.children.size();i++)
			{
				compute_best_accuracy_among_all_nodes(currentNode.children.get(i));
			}
		}
	}
	
	static boolean check_given_example(int example_index, DecisionNode rootNode, int targetAttributeIndex)
	{
		if(rootNode.attrNumber == targetAttributeIndex + 1)	// resultant classification at leaf is ">50K"
		{
			if(validationset.get(example_index)[targetAttributeIndex].equals(">50K"))
				return true;
			else
				return false;
		}
		else if(rootNode.attrNumber == targetAttributeIndex + 2)
		{
			if(validationset.get(example_index)[targetAttributeIndex].equals("<=50K"))
				return true;
			else
				return false;
		}
		else								//in this case there is a need to go deeper into the tree
		{
			String decision_node_attr_value = validationset.get(example_index)[rootNode.attrNumber];
			
			if(attributeValues.get(rootNode.attrNumber)[0].equals("continuous")) //if the current rootNode represents a continuous attribute
			{
				if(decision_node_attr_value.equals("lessThan"))
					return check_given_example(example_index, rootNode.children.get(0), targetAttributeIndex);
				else
					return check_given_example(example_index, rootNode.children.get(1), targetAttributeIndex);
					
			}
			else						//if the current rootNode represents a non-continuous attribute
			{
				for(int i=0;i<attributeValues.get(rootNode.attrNumber).length;i++)
				{
					if(decision_node_attr_value.equals(attributeValues.get(rootNode.attrNumber)[i]))
					{
						return check_given_example(example_index, rootNode.children.get(i), targetAttributeIndex);
					}
				}
			}
		}
		
		return false; //logically this is an unreachable code, but Eclipse IDE gives error without this
	}
	
	static float compute_accuracy(DecisionNode rootNode, int targetAttributeIndex)
	{
		float no_of_correct_classifications = 0;
		
		for(int i=0;i<validationset.size();i++)
		{
			if(check_given_example(i, rootNode, targetAttributeIndex))
				no_of_correct_classifications++;
		}
		
		return no_of_correct_classifications/validationset.size();
	}
	
	static float compute_precision(DecisionNode rootNode, int targetAttributeIndex)
	{
		float no_of_true_positives = 0; // if targetAttribute is ">50K", it is considered positive
		float no_of_false_positives = 0;	//  if target attribute actually is "<=50K" but prediction is ">50K"
		
		for(int i=0;i<validationset.size();i++)
		{
			if(validationset.get(i)[14].equals(">50K"))
			{
				if(check_given_example(i, rootNode, targetAttributeIndex))
					no_of_true_positives++;
			}
			else
			{
				if(!check_given_example(i, rootNode, targetAttributeIndex))
					no_of_false_positives++;
			}
		}
		
		return no_of_true_positives/(no_of_true_positives + no_of_false_positives);
	}
	
	static float compute_recall(DecisionNode rootNode, int targetAttributeIndex)		//to calculate recall for the current tree and validationset
	{
		float no_of_true_positives = 0;
		float no_of_false_negatives = 0;
		
		for(int i=0;i<validationset.size();i++)
		{
			if(validationset.get(i)[14].equals(">50K"))
			{
				if(check_given_example(i, rootNode, targetAttributeIndex))
					no_of_true_positives++;
				else
					no_of_false_negatives++;
			}
		}
		
		return no_of_true_positives/(no_of_true_positives + no_of_false_negatives);
	}
	
	static float compute_f_measure(float precision, float recall)		//to calculate f-measure for the current tree and validationset
	{
		return 2*precision*recall/(precision+recall);
	}
	
	String compute_most_probable_attr_value(int attr_index)				//computes the most probable attribute value for the current attribute (for dealing with missing data)
	{
		int number_of_values_of_attr = 0;
		
		if(attributeValues.get(attr_index)[0].equals("continuous"))
			number_of_values_of_attr = 2;
		else
			number_of_values_of_attr = attributeValues.get(attr_index).length;
		
		int count_of_each_value_of_attr[] = new int[number_of_values_of_attr];
		
		for(int i=0;i<number_of_values_of_attr;i++)
		{
			for(int j=0;j<dataset.size();j++)
			{
				if(i==0 && attributeValues.get(attr_index)[0].equals("continuous"))
				{
					if(dataset.get(j)[attr_index].equals("lessThan"))
						count_of_each_value_of_attr[i] += 1;
				}
				else if(i==1 && attributeValues.get(attr_index)[0].equals("continuous"))
				{
					if(dataset.get(j)[attr_index].equals("greaterThan"))
						count_of_each_value_of_attr[i] += 1;
				}
				else if(dataset.get(j)[attr_index].equals(attributeValues.get(attr_index)[i]))
					count_of_each_value_of_attr[i] += 1;
			}
		}
		
		int max_count = 0;
		int most_probable_value_index = 0;
		
		for(int i=0;i<number_of_values_of_attr;i++)
		{
			if(max_count < count_of_each_value_of_attr[i])
			{
				max_count = count_of_each_value_of_attr[i];
				most_probable_value_index = i;
			}
		}
		
		if(attributeValues.get(attr_index)[0].equals("continuous"))
		{
			if(most_probable_value_index == 0)
				return "lessThan";
			else
				return "greaterThan";
		}
		else
			return attributeValues.get(attr_index)[most_probable_value_index];
	}
	
	String compute_most_probable_attr_value2(int attr_index)		//for validationset
	{
		int number_of_values_of_attr = 0;
		
		if(attributeValues.get(attr_index)[0].equals("continuous"))
			number_of_values_of_attr = 2;
		else
			number_of_values_of_attr = attributeValues.get(attr_index).length;
		
		int count_of_each_value_of_attr[] = new int[number_of_values_of_attr];
		
		for(int i=0;i<number_of_values_of_attr;i++)
		{
			for(int j=0;j<validationset.size();j++)
			{
				if(i==0 && attributeValues.get(attr_index)[0].equals("continuous"))
				{
					if(validationset.get(j)[attr_index].equals("lessThan"))
						count_of_each_value_of_attr[i] += 1;
				}
				else if(i==1 && attributeValues.get(attr_index)[0].equals("continuous"))
				{
					if(validationset.get(j)[attr_index].equals("greaterThan"))
						count_of_each_value_of_attr[i] += 1;
				}
				else if(validationset.get(j)[attr_index].equals(attributeValues.get(attr_index)[i]))
					count_of_each_value_of_attr[i] += 1;
			}
		}
		
		int max_count = 0;
		int most_probable_value_index = 0;
		
		for(int i=0;i<number_of_values_of_attr;i++)
		{
			if(max_count < count_of_each_value_of_attr[i])
			{
				max_count = count_of_each_value_of_attr[i];
				most_probable_value_index = i;
			}
		}
		
		if(attributeValues.get(attr_index)[0].equals("continuous"))
		{
			if(most_probable_value_index == 0)
				return "lessThan";
			else
				return "greaterThan";
		}
		else
			return attributeValues.get(attr_index)[most_probable_value_index];
	}
	
	float compute_entropy_with_given_attr(int datasetIndexes[], int attr_index)
	{
		int number_of_values_of_attr = 0;
		
		if(attributeValues.get(attr_index)[0].equals("continuous"))
			number_of_values_of_attr = 2;
		else
			number_of_values_of_attr = attributeValues.get(attr_index).length;
		
		float count_of_each_value_of_attr[] = new float[number_of_values_of_attr];
		float count_of_more_than_50K_for_given_value[] = new float[number_of_values_of_attr];	//count of examples having more than 50K having the given attribute value
		
		for(int i=0;i<number_of_values_of_attr;i++)
		{
			for(int j=0;j<datasetIndexes.length;j++)
			{
				if(i==0 && attributeValues.get(attr_index)[0].equals("continuous"))
				{
					if(dataset.get(datasetIndexes[j])[attr_index].equals("lessThan"))
					{
						count_of_each_value_of_attr[i] += 1;
						
						if(dataset.get(datasetIndexes[j])[14].equals(">50K"))
							count_of_more_than_50K_for_given_value[i] += 1;
					}
				}
				else if(i==1 && attributeValues.get(attr_index)[0].equals("continuous"))
				{
					if(dataset.get(datasetIndexes[j])[attr_index].equals("greaterThan"))
					{
						count_of_each_value_of_attr[i] += 1;
						
						if(dataset.get(datasetIndexes[j])[14].equals(">50K"))
							count_of_more_than_50K_for_given_value[i] += 1;
					}
				}
				else if(dataset.get(datasetIndexes[j])[attr_index].equals(attributeValues.get(attr_index)[i]))
				{
					count_of_each_value_of_attr[i] += 1;
					
					if(dataset.get(datasetIndexes[j])[14].equals(">50K"))
						count_of_more_than_50K_for_given_value[i] += 1;
				}
			}	
		}
		
		float entropy = 0;
		
		for(int i=0;i<number_of_values_of_attr;i++)
		{
			if(count_of_more_than_50K_for_given_value[i] != 0)
			{
				entropy += (count_of_each_value_of_attr[i]/datasetIndexes.length) * (count_of_more_than_50K_for_given_value[i]/count_of_each_value_of_attr[i]) * (-Math.log(count_of_more_than_50K_for_given_value[i]/count_of_each_value_of_attr[i]));
			}
			
			if(count_of_each_value_of_attr[i] - count_of_more_than_50K_for_given_value[i] != 0)
			{
				entropy += (count_of_each_value_of_attr[i]/datasetIndexes.length) * (1 - count_of_more_than_50K_for_given_value[i]/count_of_each_value_of_attr[i]) * (-Math.log(1 - count_of_more_than_50K_for_given_value[i]/count_of_each_value_of_attr[i]));
			}
		}
		
		return entropy;
	}
	
	float computeEntropy(ContAttr arr[], float c, int count)
	{
		float entropy = 0;
		float less_than_count = 0;	//case A
		float greater_than_or_equal_to_count = 0;	//case B
		float less_than_or_equal_to_50K_in_A = 0;
		float less_than_or_equal_to_50K_in_B = 0;
		
		for(int i=0;i<count;i++)
		{
			if(arr[i].value < c)
			{
				less_than_count++;
				
				if(arr[i].targetAttrValue.equals("<=50K"))
					less_than_or_equal_to_50K_in_A++;
			}
			else
			{
				greater_than_or_equal_to_count++;
				
				if(arr[i].targetAttrValue.equals("<=50K"))
					less_than_or_equal_to_50K_in_B++;
			}
		}	
			
		if(less_than_or_equal_to_50K_in_A != 0)
		{
			entropy += (less_than_count/count) * (less_than_or_equal_to_50K_in_A/less_than_count) * (-Math.log(less_than_or_equal_to_50K_in_A/less_than_count));
		}
		
		if(less_than_count - less_than_or_equal_to_50K_in_A != 0)
		{
			entropy += (less_than_count/count) * (1 - less_than_or_equal_to_50K_in_A/less_than_count) * (-Math.log(1 - less_than_or_equal_to_50K_in_A/less_than_count));
		}
		
		if(less_than_or_equal_to_50K_in_B != 0)
		{
			entropy += (1 - less_than_count/count) * (less_than_or_equal_to_50K_in_B/greater_than_or_equal_to_count) * (-Math.log(less_than_or_equal_to_50K_in_B/greater_than_or_equal_to_count));				
		}
		
		if(greater_than_or_equal_to_count - less_than_or_equal_to_50K_in_B != 0)
		{
			entropy += (1 - less_than_count/count) * (1 - less_than_or_equal_to_50K_in_B/greater_than_or_equal_to_count) * (-Math.log(1 - less_than_or_equal_to_50K_in_B/greater_than_or_equal_to_count));
		}
		
		return entropy;
	}
	
	static int random_integer(int min, int max)	//min and max both inclusive
	{
		Random ran = new Random();
		int x = ran.nextInt((max-min)+1) + min;
		
		return x;
	}
	
	void make_tree(DecisionNode currentNode, int datasetIndexes[], int attrIndexes[], int targetAttrIndex, int is_random_forest_tree)
	{
		int no_of_examples = datasetIndexes.length;
		int no_of_attr;
		
		if(attrIndexes == null)
			no_of_attr = 0;
		else
			no_of_attr = attrIndexes.length;
		
		int more_than_50K = 0;
		int less_than_or_equal_to_50K = 0;
		
		for(int i=0;i<no_of_examples;i++)
		{
			if(dataset.get(datasetIndexes[i])[14].equals(">50K"))
				more_than_50K++;
			else
				less_than_or_equal_to_50K++;
		}
		
		if(more_than_50K > less_than_or_equal_to_50K)
			currentNode.most_probable_target_value_if_pruned = targetAttrIndex + 1;
		else
			currentNode.most_probable_target_value_if_pruned = targetAttrIndex + 2;
		
		if(more_than_50K == no_of_examples)
			currentNode.attrNumber = targetAttrIndex + 1;
		else if(more_than_50K == 0)
			currentNode.attrNumber = targetAttrIndex + 2;
		else if(no_of_attr == 0)
		{
			if(more_than_50K > less_than_or_equal_to_50K)
				currentNode.attrNumber = targetAttrIndex + 1;
			else
				currentNode.attrNumber = targetAttrIndex + 2;
		}
		else
		{
			if(is_random_forest_tree == 0)				//code to be executed for ID3
			{
				//we now need to find the attribute that best classifies the current examples
				float entropy = 0;
				int attr_index = 0;	// for the attribute that best classifies the current examples
				
				for(int i=0;i<no_of_attr;i++)
				{
					if(i==0)
					{
						entropy = compute_entropy_with_given_attr(datasetIndexes, attrIndexes[i]);
						attr_index = attrIndexes[i];
					}
					else
					{
						float new_entropy = compute_entropy_with_given_attr(datasetIndexes, attrIndexes[i]);
						
						if(entropy>new_entropy)
						{
							entropy = new_entropy;
							attr_index = attrIndexes[i];
						}
					}
				}
				
				currentNode.attrNumber = attr_index;
				
				if(attributeValues.get(attr_index)[0].equals("continuous"))		//code to be executed for continuous attributes
				{
					for(int i=0;i<2;i++)	//create a child for all possible values of attribute at currentNode
					{
						DecisionNode child = new DecisionNode();	//create a child
						currentNode.children.add(child);
						int no_of_examples_for_child = 0;
						
						for(int j=0;j<no_of_examples;j++)
						{
							if(i==0)
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("lessThan"))
									no_of_examples_for_child++;
							}
							else
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("greaterThan"))
									no_of_examples_for_child++;
							}
						}
						
						int example_indexes_for_child[] = new int[no_of_examples_for_child];
						
						int next = 0;	//location where the next matching example index is to be put in example_indexes_for_child[]
						
						for(int j=0;j<no_of_examples;j++)
						{
							if(i==0)
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("lessThan"))
								{
									example_indexes_for_child[next] = datasetIndexes[j];
									next++;
								}
							}
							else
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("greaterThan"))
								{
									example_indexes_for_child[next] = datasetIndexes[j];
									next++;
								}
							}
						}
						
						if(no_of_attr - 1 == 0)
							make_tree(child, example_indexes_for_child, null, targetAttrIndex, 0);
						else
						{
							int attr_indexes_for_child[] = new int[no_of_attr - 1];
							next = 0;	//location where the next attr index is to be put in attr_indexes_for_child[];
							
							for(int j=0;j<no_of_attr;j++)
							{
								if(attrIndexes[j] != attr_index)
								{
									attr_indexes_for_child[next] = attrIndexes[j];
									next++;
								}
							}
							
							make_tree(child, example_indexes_for_child, attr_indexes_for_child, targetAttrIndex, 0);
						}
					}
				}
				else									//code to be executed for discontinuous attributes
				{
					for(int i=0;i<attributeValues.get(attr_index).length;i++)	//create a child for all possible values of attribute at currentNode
					{
						DecisionNode child = new DecisionNode();	//create a child
						currentNode.children.add(child);
						int no_of_examples_for_child = 0;
						
						for(int j=0;j<no_of_examples;j++)
							if(dataset.get(datasetIndexes[j])[attr_index].equals(attributeValues.get(attr_index)[i]))
								no_of_examples_for_child++;
						
						int example_indexes_for_child[] = new int[no_of_examples_for_child];
						
						int next = 0;	//location where the next matching example index is to be put in example_indexes_for_child[]
						
						for(int j=0;j<no_of_examples;j++)
							if(dataset.get(datasetIndexes[j])[attr_index].equals(attributeValues.get(attr_index)[i]))
							{
								example_indexes_for_child[next] = datasetIndexes[j];
								next++;
							}
						
						if(no_of_attr - 1 == 0)
							make_tree(child, example_indexes_for_child, null, targetAttrIndex, 0);
						else
						{
							int attr_indexes_for_child[] = new int[no_of_attr - 1];
							next = 0;	//location where the next attr index is to be put in attr_indexes_for_child[];
							
							for(int j=0;j<no_of_attr;j++)
							{
								if(attrIndexes[j] != attr_index)
								{
									attr_indexes_for_child[next] = attrIndexes[j];
									next++;
								}
							}
							
							make_tree(child, example_indexes_for_child, attr_indexes_for_child, targetAttrIndex, 0);
						}
					}
				}
			}
			else	//code to be executed for randomforest
			{
				int no_of_attr_new = (int) (Math.log(no_of_attr) / Math.log(2));
				
				int attrIndexesNew[] = new int[no_of_attr_new];
				
				ArrayList<Integer> shuffle = new ArrayList<Integer>();
				
				for(int i=0;i<no_of_attr;i++)
					shuffle.add(new Integer(attrIndexes[i]));
				
				Collections.shuffle(shuffle);
				
				for(int i=0;i<no_of_attr_new;i++)
					attrIndexesNew[i] = shuffle.get(i).intValue();
				
				//we now need to find the attribute that best classifies the current examples
				float entropy = 0;
				int attr_index = 0;	// for the attribute that best classifies the current examples
				
				for(int i=0;i<no_of_attr_new;i++)
				{
					if(i==0)
					{
						entropy = compute_entropy_with_given_attr(datasetIndexes, attrIndexesNew[i]);
						attr_index = attrIndexesNew[i];
					}
					else
					{
						float new_entropy = compute_entropy_with_given_attr(datasetIndexes, attrIndexesNew[i]);
						
						if(entropy>new_entropy)
						{
							entropy = new_entropy;
							attr_index = attrIndexesNew[i];
						}
					}
				}
				
				currentNode.attrNumber = attr_index;
				
				if(attributeValues.get(attr_index)[0].equals("continuous"))		//code to be executed for continuous attributes
				{
					for(int i=0;i<2;i++)	//create a child for all possible values of attribute at currentNode
					{
						DecisionNode child = new DecisionNode();	//create a child
						currentNode.children.add(child);
						int no_of_examples_for_child = 0;
						
						for(int j=0;j<no_of_examples;j++)
						{
							if(i==0)
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("lessThan"))
									no_of_examples_for_child++;
							}
							else
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("greaterThan"))
									no_of_examples_for_child++;
							}
						}
						
						int example_indexes_for_child[] = new int[no_of_examples_for_child];
						
						int next = 0;	//location where the next matching example index is to be put in example_indexes_for_child[]
						
						for(int j=0;j<no_of_examples;j++)
						{
							if(i==0)
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("lessThan"))
								{
									example_indexes_for_child[next] = datasetIndexes[j];
									next++;
								}
							}
							else
							{
								if(dataset.get(datasetIndexes[j])[attr_index].equals("greaterThan"))
								{
									example_indexes_for_child[next] = datasetIndexes[j];
									next++;
								}
							}
						}
						
						if(no_of_attr - 1 == 0)
							make_tree(child, example_indexes_for_child, null, targetAttrIndex, 1);
						else
						{
							int attr_indexes_for_child[] = new int[no_of_attr - 1];
							next = 0;	//location where the next attr index is to be put in attr_indexes_for_child[];
							
							for(int j=0;j<no_of_attr;j++)
							{
								if(attrIndexes[j] != attr_index)
								{
									attr_indexes_for_child[next] = attrIndexes[j];
									next++;
								}
							}
							
							make_tree(child, example_indexes_for_child, attr_indexes_for_child, targetAttrIndex, 1);
						}
					}
				}
				else								//code to be executed for discontinuous attributes
				{
					for(int i=0;i<attributeValues.get(attr_index).length;i++)	//create a child for all possible values of attribute at currentNode
					{
						DecisionNode child = new DecisionNode();	//create a child
						currentNode.children.add(child);
						int no_of_examples_for_child = 0;
						
						for(int j=0;j<no_of_examples;j++)
							if(dataset.get(datasetIndexes[j])[attr_index].equals(attributeValues.get(attr_index)[i]))
								no_of_examples_for_child++;
						
						int example_indexes_for_child[] = new int[no_of_examples_for_child];
						
						int next = 0;	//location where the next matching example index is to be put in example_indexes_for_child[]
						
						for(int j=0;j<no_of_examples;j++)
							if(dataset.get(datasetIndexes[j])[attr_index].equals(attributeValues.get(attr_index)[i]))
							{
								example_indexes_for_child[next] = datasetIndexes[j];
								next++;
							}
						
						if(no_of_attr - 1 == 0)
							make_tree(child, example_indexes_for_child, null, targetAttrIndex, 1);
						else
						{
							int attr_indexes_for_child[] = new int[no_of_attr - 1];
							next = 0;	//location where the next attr index is to be put in attr_indexes_for_child[];
							
							for(int j=0;j<no_of_attr;j++)
							{
								if(attrIndexes[j] != attr_index)
								{
									attr_indexes_for_child[next] = attrIndexes[j];
									next++;
								}
							}
							
							make_tree(child, example_indexes_for_child, attr_indexes_for_child, targetAttrIndex, 1);
						}
					}
				}
			}
		}
	}
	
	public static void main(String args[])throws Exception
	{
		DecisionTree tree = new DecisionTree();
		
		File f1 = new File("adult.data.txt");
		File f2 = new File("list_of_attribute_values.txt");
		File f3 = new File("adult.test.txt");
		BufferedReader br1 = new BufferedReader(new FileReader(f1));
		BufferedReader br2 = new BufferedReader(new FileReader(f2));
		BufferedReader br3 = new BufferedReader(new FileReader(f3));
		
		String line;
		
		while((line = br1.readLine())!=null)
			dataset.add(line.split(", "));	//added the training examples one by one to the dataset
		
		br1.close();
		
		while((line = br2.readLine())!=null)
			attributeValues.add(line.split(", "));	//added the values each attribute can take
		
		br2.close();
		
		while((line = br3.readLine())!=null)
			validationset.add(line.split(", "));	//added the testing examples one by one to the validationset
		
		br3.close();
		
		int no_of_attr = attributeValues.size();
		int no_of_examples = dataset.size();
		
		int attrIndexes[] = new int[no_of_attr];	//will store the indexes of attributes under consideration, is used when make_tree() is called.
		int datasetIndexes[] = new int[no_of_examples];	//will store the indexes of examples under consideration, is used when make_tree() is called.
		
		//first we deal with the discretization of continuous values, this for LOOP does exactly that
		for(int i=0;i<no_of_attr;i++)
		{
			attrIndexes[i] = i; 
			
			if(attributeValues.get(i)[0].equals("continuous"))
			{
				ContAttr arr[] = new ContAttr[no_of_examples];	//continuous attribute's value for each example
				int k = 0;
				
				for(int j=0;j<no_of_examples;j++)
					if(!dataset.get(j)[i].equals("?"))
					{
						arr[k] = new ContAttr();
						arr[k].value = Integer.parseInt(dataset.get(j)[i]);
						arr[k].targetAttrValue = dataset.get(j)[no_of_attr];
						k++;
					}
				
				Arrays.sort(arr, 0, k, new SortByValue());
				
				ArrayList<Float> cS = new ArrayList<Float>();
				
				for(int j=0;j<k-1;j++)
				{
					if(!arr[j].targetAttrValue.equals(arr[j+1].targetAttrValue))
						cS.add(new Float("" + (arr[j].value + arr[j+1].value)/2.0)); //can only add objects so float type is added as Float object, its value can be obtained as obj.floatValue();
				}
						
				// we need to get that c for which the new entropy is minimized
				float entropy = 0;
				float c = 0;
				
				for(int j=0;j<cS.size();j++)
				{
					if(j==0)
					{
						entropy = tree.computeEntropy(arr, cS.get(j).floatValue(), k);
						c = cS.get(j).floatValue();
					}
					
					else
					{
						float new_entropy = tree.computeEntropy(arr, cS.get(j).floatValue(), k);
						
						if(entropy>new_entropy)
						{
							entropy = new_entropy;
							c = cS.get(j).floatValue();
						}
					}
				}
				
				for(int j=0;j<no_of_examples;j++)
				{
					if(!dataset.get(j)[i].equals("?"))
					{
						if(Integer.parseInt(dataset.get(j)[i]) < c)
							dataset.get(j)[i] = "lessThan";
						else
							dataset.get(j)[i] = "greaterThan";
					}
				}
						
			}
		}
		
		//now we need to deal with missing values, replacing them with the most_probable_attr_value
		
		ArrayList<String> most_probable_attr_value = new ArrayList<String>(); //this will store the most probable attribute value for each attribute, now that all attributes are discrete
		
		for(int i=0;i<no_of_attr;i++)
		{
			most_probable_attr_value.add(tree.compute_most_probable_attr_value(i));
			
			for(int j=0;j<no_of_examples;j++)
			{
				if(dataset.get(j)[i].equals("?"))
					dataset.get(j)[i] = most_probable_attr_value.get(i);
			}
		}
		
		//discretization and missing value handling code for validation set starts here-------------------------------------------------
		
		int no_of_examples2 = validationset.size();
		
		//first we deal with the discretization of continuous values, this for LOOP does exactly that
				for(int i=0;i<no_of_attr;i++)
				{
					attrIndexes[i] = i; 
					
					if(attributeValues.get(i)[0].equals("continuous"))
					{
						ContAttr arr[] = new ContAttr[no_of_examples];	//continuous attribute's value for each example
						int k = 0;
						
						for(int j=0;j<no_of_examples2;j++)
							if(!validationset.get(j)[i].equals("?"))
							{
								arr[k] = new ContAttr();
								arr[k].value = Integer.parseInt(validationset.get(j)[i]);
								arr[k].targetAttrValue = validationset.get(j)[no_of_attr];
								k++;
							}
						
						Arrays.sort(arr, 0, k, new SortByValue());
						
						ArrayList<Float> cS = new ArrayList<Float>();
						
						for(int j=0;j<k-1;j++)
						{
							if(!arr[j].targetAttrValue.equals(arr[j+1].targetAttrValue))
								cS.add(new Float("" + (arr[j].value + arr[j+1].value)/2.0)); //can only add objects so float type is added as Float object, its value can be obtained as obj.floatValue();
						}
								
						// we need to get that c for which the new entropy is minimized
						float entropy = 0;
						float c = 0;
						
						for(int j=0;j<cS.size();j++)
						{
							if(j==0)
							{
								entropy = tree.computeEntropy(arr, cS.get(j).floatValue(), k);
								c = cS.get(j).floatValue();
							}
							
							else
							{
								float new_entropy = tree.computeEntropy(arr, cS.get(j).floatValue(), k);
								
								if(entropy>new_entropy)
								{
									entropy = new_entropy;
									c = cS.get(j).floatValue();
								}
							}
						}
						
						for(int j=0;j<no_of_examples2;j++)
						{
							if(!validationset.get(j)[i].equals("?"))
							{
								if(Integer.parseInt(validationset.get(j)[i]) < c)
									validationset.get(j)[i] = "lessThan";
								else
									validationset.get(j)[i] = "greaterThan";
							}
						}
								
					}
				}
				
				//now we need to deal with missing values, replacing them with the most_probable_attr_value
				
				ArrayList<String> most_probable_attr_value2 = new ArrayList<String>(); //this will store the most probable attribute value for each attribute, now that all attributes are discrete
				
				for(int i=0;i<no_of_attr;i++)
				{
					most_probable_attr_value2.add(tree.compute_most_probable_attr_value2(i));
					
					for(int j=0;j<no_of_examples2;j++)
					{
						if(validationset.get(j)[i].equals("?"))
							validationset.get(j)[i] = most_probable_attr_value2.get(i);
					}
				}
		
		//discretization and missing value handling code for validation set ends here-----------------------------------------------------
		
		//now we need to start the actual ID3 algorithm as the data is now discretized and complete
		
		for(int i=0;i<no_of_examples;i++)
			datasetIndexes[i] = i;
		
		long startTime = System.currentTimeMillis();
		
		tree.make_tree(tree.root, datasetIndexes, attrIndexes, 14, 0);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		
		System.out.println("Program successfully completed creating tree using ID3!!! :)");
		
		System.out.println("Accuracy of ID3 is "+compute_accuracy(tree.root, 14));
		
		float precision = compute_precision(tree.root, 14);
		
		System.out.println("Precision of ID3 is "+precision);
		
		float recall = compute_recall(tree.root, 14);
		
		System.out.println("Recall of ID3 is "+recall);
		
		System.out.println("F-Measure of ID3 is "+compute_f_measure(precision, recall));
		
		System.out.println("Total time taken to make tree using ID3 is "+estimatedTime/1000+" secs");
		
		//code for creating random forest
		int no_of_trees_in_random_forest = 10;
		DecisionTree random_forest[] = new DecisionTree[no_of_trees_in_random_forest];	//for the random forest
		
		startTime = System.currentTimeMillis();
		
		for(int i=0;i<no_of_trees_in_random_forest;i++)				//to generate the random dataset for each tree in the forest
		{
			random_forest[i] = new DecisionTree();
			
			int datasetIndexes2[] = new int[no_of_examples]; 
			
			for(int j=0;j<no_of_examples;j++)
			{
				datasetIndexes2[j] = random_integer(0, no_of_examples-1);
			}
			
			random_forest[i].make_tree(random_forest[i].root, datasetIndexes2, attrIndexes, 14, 1);
		}
		
		estimatedTime = System.currentTimeMillis() - startTime;
		
		System.out.println("Program successfully completed creating random forest having "+no_of_trees_in_random_forest+" trees!!! :)");
		
		float accuracy, f_measure;
		
		float no_of_correct_classifications = 0;	//for computing accuracy
		float no_of_true_positives = 0;
		float no_of_false_positives = 0;
		float no_of_true_negatives = 0;
		float no_of_false_negatives = 0;
		
		for(int i=0;i<validationset.size();i++)
		{
			int false_predictions = 0;
			int true_predictions = 0;
			
			for(int j=0;j<no_of_trees_in_random_forest;j++)
			{
				if(check_given_example(i, random_forest[j].root, 14))
					true_predictions++;
				else
					false_predictions++;
			}
			
			if(true_predictions > false_predictions)
				no_of_correct_classifications++;
			
			if(validationset.get(i)[14].equals(">50K"))
			{
				if(true_predictions > false_predictions)
					no_of_true_positives++;
				else
					no_of_false_negatives++;
			}
			else
			{
				if(true_predictions > false_predictions)
					no_of_true_negatives++;
				else
					no_of_false_positives++;
			}
		}
		
		accuracy = no_of_correct_classifications/validationset.size();	//accuracy of random-forest
		precision = no_of_true_positives/(no_of_true_positives + no_of_false_positives);
		recall = no_of_true_positives/(no_of_true_positives + no_of_false_negatives);
		f_measure = 2*precision*recall/(precision + recall);
		
		System.out.println("Accuracy of random-forest is "+accuracy);
		System.out.println("Precision of random-forest is "+precision);
		System.out.println("Recall of random-forest is "+recall);
		System.out.println("F-Measure of random-forest is "+f_measure);
		System.out.println("Total time taken to make trees in random-forest is "+estimatedTime/1000+" secs");
		
		//code for creating pruned tree by reduced error pruning
		
		//first compute the original accuracy
		accuracy = compute_accuracy(tree.root, 14);	//accuracy of the current tree before pruning
		
		startTime = System.currentTimeMillis();
		
		int nodes_pruned_till_now = 0;
		int no_of_nodes_to_be_pruned = 1;
		
		while(nodes_pruned_till_now < no_of_nodes_to_be_pruned)
		{
			tree.next_best_accuracy = 0;
			tree.compute_best_accuracy_among_all_nodes(tree.root);
			
			System.out.println("Number of nodes pruned till now is "+nodes_pruned_till_now);
			
			if(tree.next_best_accuracy > accuracy)
			{
				tree.best_accuracy_node.attrNumber = tree.best_accuracy_node.most_probable_target_value_if_pruned; //basically, here the best_accuracy_node is being pruned
				accuracy = tree.next_best_accuracy;
				nodes_pruned_till_now++;
			}
			else
				break;
		}
		
		estimatedTime = System.currentTimeMillis() - startTime;
		
		System.out.println("Program successfully completed creating pruned tree by pruning "+no_of_nodes_to_be_pruned+" nodes!!! :)");
		
		System.out.println("Accuracy of pruned-tree is "+compute_accuracy(tree.root, 14));
		
		precision = compute_precision(tree.root, 14);
		
		System.out.println("Precision of pruned-tree is "+precision);
		
		recall = compute_recall(tree.root, 14);
		
		System.out.println("Recall of pruned-tree is "+recall);
		
		System.out.println("F-Measure of pruned-tree is "+compute_f_measure(precision, recall));
		
		System.out.println("Total time taken to make pruned-tree is "+estimatedTime/1000+" secs");
	}
}
