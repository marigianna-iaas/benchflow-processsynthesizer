package de.unistuttgart.iaas.newbpmnprocess.composer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import de.unistuttgart.iaas.newbpmnprocess.database.DBConnection;
 
public class SizeCombinationsCalculator {
 	

    //FIXME: this method can be splitted - organized better
	//FIXME: add permutations for the combinations so that we really get every available combination - maybe it is too much
    public static ArrayList<ArrayList<Integer>> getValidSizeCombinations(int totalSize, int noOfCriteria, int minAllowedNumberOfNodes)
    {
    	ArrayList<Integer> sizesToCombine = getAvailableSizes(totalSize, noOfCriteria, minAllowedNumberOfNodes);
    	int [] sizesToCombineArr = new int[sizesToCombine.size()];
    	int i=0;
    	Arrays.sort(sizesToCombineArr);
    	ArrayList<ArrayList<Integer>> combinationsList =  new ArrayList<ArrayList<Integer>>();
    	ArrayList<ArrayList<Integer>> newCombinationsList  = new ArrayList<ArrayList<Integer>>();

    	for(Integer size : sizesToCombine)
    	{
    		sizesToCombineArr[i++] = size.intValue();

    	}
    	combinationsList = combinationUtil(sizesToCombineArr, new int[noOfCriteria], 0, sizesToCombineArr.length - 1, 0,  noOfCriteria, combinationsList);
    	combinationsList = makeSelfCombinations(sizesToCombineArr, noOfCriteria, combinationsList);
    	int sum = 0;

    	for(ArrayList<Integer> combination : combinationsList)
    	{
    		for(Integer c : combination)
    		{
    			sum += c.intValue();
    		}
    		if(sum  == totalSize)
    		{
    			newCombinationsList.add(combination);   	
    		}
    		
    		sum = 0;
    	}
    	return newCombinationsList;
    }

    private static ArrayList<ArrayList<Integer>> makeSelfCombinations(
			int[] sizesToCombineArr, int noOfCriteria , ArrayList<ArrayList<Integer>> combinationsList) {
		ArrayList<Integer> combination = new ArrayList<Integer>();
		for(int i=0; i< sizesToCombineArr.length; i++)
		{
			for(int j=0; j< noOfCriteria; j++)
	    	{
	    		combination.add(sizesToCombineArr[i]);
	    	}
			combinationsList.add(combination);
			combination = new ArrayList<Integer>();
		}
    	
    	return combinationsList;
	}

	/* arr[]  ---> Input Array
    data[] ---> Temporary array to store current combination
    start & end ---> Staring and Ending indexes in arr[]
    index  ---> Current index in data[]
    r ---> Size of a combination to be printed */
    static ArrayList<ArrayList<Integer>> combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r, ArrayList<ArrayList<Integer>> result)
    {
        // Current combination is ready to be printed, print it
        if (index == r)
        {
        	result.add(new ArrayList<Integer>());
        	int combinationIndex = result.size() - 1;
            for (int j=0; j<r; j++)
            {
            	result.get(combinationIndex).add(Integer.valueOf(data[j]));
            }
            return result;
        }
 
        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {
            data[index] = arr[i];
            result = combinationUtil(arr, data, i+1, end, index+1, r, result);
        }
        return result;
    }
 

    
    private static ArrayList<Integer> getAvailableSizes(int totalSize, int noOfCriteria, int minAllowedNumberOfNodes) {
		DBConnection connec = new DBConnection();
		ResultSet resultSizes;
		ArrayList<Integer> resultSizesArr = new ArrayList<Integer>();
		
		int maxSizeAllowed = totalSize - minAllowedNumberOfNodes * (noOfCriteria-1);			
		
		String selectFragmentSql = "SELECT DISTINCT numberOfFlowNodes as size FROM `fragments` WHERE numberOfFlowNodes BETWEEN "+ minAllowedNumberOfNodes +" AND " + maxSizeAllowed ;
		// selects all fragments that comply with specific criteria
		resultSizes = connec.selectData(selectFragmentSql);
		resultSizesArr = new ArrayList<Integer>();
		try {
			while (resultSizes.next()) {
				resultSizesArr.add(resultSizes.getInt("size"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		connec.closeConnection();

		return resultSizesArr;

	}

    
}