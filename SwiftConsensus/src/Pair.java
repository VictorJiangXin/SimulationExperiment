
/**
 * 只是编程中需要，因为每个节点有个对应的index，
 * 但是循环坐标中，对应节点index可以是负数表示，
 * 因此用一个该对象，存储
 * @author xinjiang
 *
 */
public class Pair implements Comparable<Pair>{
	public int nodeId;
	public int index;
	
	
	public Pair(int nodeId, int index) {
		this.nodeId = nodeId;
		this.index = index;
	}
	
	
	@Override
	public int compareTo(Pair p) {
		return index - p.index;
	}
	
	
	@Override
	public String toString() {
		String s;
		s = "Pair<" + nodeId + ", " + index +">";
		return s;
	}
}
