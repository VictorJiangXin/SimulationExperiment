
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
