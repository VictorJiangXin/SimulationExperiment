
/**
 * 用于表示发送的交易包
 * 主要包括：
 * 	1. 交易本身的ID
 * 	2. 交易包在发送过程中会被复制，但始终有个统计类对象，记录其传播中的一些数据
 * 	3. 交易需要传播的坐标范围
 * 	4. 交易最初始发送的节点ID
 * 	5. 由于交易在网络中以树状形式，传播，需要记录其深度
 * 
 * 在传播过程中，节点需要复制该交易包，将其发送到其他节点，因此需要复制函数
 * @author xinjiang
 *
 */
public class TxPackage {
	private int txId;
	private int originNodeId;
	private Statistic statistic;
	private int leftBound;
	private int rightBound;
	private int depth;
	
	
	public TxPackage(
			int txId,
			int originNodeId,
			Statistic statistic,
			int leftBound,
			int rightBound,
			int depth) {
		this.txId = txId;
		this.originNodeId = originNodeId;
		this.statistic = statistic;
		this.leftBound = leftBound;
		this.rightBound = rightBound;
		this.depth = depth;
	}
	
	
	public int getTxId() {
		return txId;
	}
	
	
	public int getOriginNodeId() {
		return originNodeId;
	}
	
	
	public Statistic getStatistic() {
		return statistic;
	}
	
	
	public int getDepth() {
		return depth;
	}
	
	
	public int getLeftBound() {
		return leftBound;
	}
	
	
	public int getRightBound() {
		return rightBound;
	}
	
	
	public TxPackage copyTxPackage(int newBoundLeft, int newBoundRight) {
		// 每复制一次，说明需要往下在传播一层，下一层的交易的深度加1
		return new TxPackage(txId, originNodeId, statistic, newBoundLeft, newBoundRight, depth + 1);
	}
}
