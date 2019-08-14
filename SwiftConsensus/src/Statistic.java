import java.util.Arrays;

/**
 * 实现全网的数据统计功能
 * 主要统计这些数据
 * 1. 节点访问标志，记录节点是否已经接收过该数据
 * 2. 记录收到数据包的节点数
 * 3. 最大传播深度
 * 4. 覆盖率到达95%时的传播深度
 * 5. 记录每一层的传播节点数目，用于查看数据
 * 6. 为传递该笔交易，全网发包总数
 * 
 * 需要实现的功能是：
 * 在传递到一个节点后，更新相应的统计量
 * @author xinjiang
 *
 */
public class Statistic {
	private boolean recievedFlag[];	// 节点标志，标志是否接收到交易
	private int layerNodeNum[];		// 每一层收到节点的个数
	private int recievedNodeNum;	// 已经接收到交易的节点数目
	private int txPackageNum;		// 全网共发送的交易包的数目
	private int hop;				// 当前传递到的最大深度
	private int delay;				// 覆盖率达到95%时的传播深度
	
	
	public Statistic() {
		recievedFlag = new boolean[Config.NODE_NUM];
		Arrays.fill(recievedFlag, false);
		
		layerNodeNum = new int[200];	//只记录前200层的
		Arrays.fill(layerNodeNum, 0);
		
		recievedNodeNum = 0;
		txPackageNum = 0;
		hop = 1;
		delay = 0;
	}
	
	public int getTxPackageNum() {
		return txPackageNum;
	}
	
	
	public int getDelay() {
		return delay;
	}
	
	
	public int getCoverage() {
		return recievedNodeNum * 100 / Config.NODE_NUM;
	}
	
	
	// 到达一个节点后，就要更新统计量，如果从未接收过交易，返回true
	public boolean update(Node n) {
		boolean flag = false;
		if (n.getTxPackage().getDepth() <= 200)
			layerNodeNum[n.getTxPackage().getDepth() - 1]++;
		txPackageNum++;
		hop = Math.max(hop,  n.getTxPackage().getDepth());
		
		// 如果之前未访问过该节点，则需要进行一些处理
		if (!recievedFlag[n.getNodeId()]) {
			recievedFlag[n.getNodeId()] = true;
			recievedNodeNum++;
			if (recievedNodeNum == Config.NODE_NUM * 0.95)
				delay = hop;
			flag = true;
		}
		return flag;
	}
}
