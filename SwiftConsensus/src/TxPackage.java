import java.util.List;

/**
 * 交易包，包括发包节点地址
 * 多跳数，发送时节点分区所在的ID范围
 * @author xinjiang
 *
 */
public class TxPackage {
	private int txId;
	private int fromNodeId;
	private Statistic statistic;
	private int boundLeft;
	private int boundRight;
	private int depth;
	
	public TxPackage(
			int txId,
			int fromNodeId,
			Statistic statistic,
			int boundLeft,
			int boundRight,
			int depth) {
		this.txId = txId;
		this.fromNodeId = fromNodeId;
		this.statistic = statistic;
		this.boundLeft = boundLeft;
		this.boundRight = boundRight;
		this.depth = depth;
	}
	
	public Statistic getStatistic() {
		return statistic;
	}

	public TxPackage copy(int boundLeft, int boundRight) {
		return new TxPackage(this.txId, this.fromNodeId, this.statistic, boundLeft, boundRight, this.depth + 1);
	}
	
	public int getBoundLeft() {
		return boundLeft;
	}
	
	public int getBoundRight() {
		return boundRight;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void printResult() {
		String result;
		result = "txId: " + txId + " " + "fromId: " + fromNodeId + " " + statistic.getResult();
		System.out.println(result);
	}
}
