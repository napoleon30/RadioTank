package cn.sharelink.use;

import android.util.Log;

/**
 * 
 * 发送间隔50MS，一次发送6个BYTE，
 * 
 * BYTE[0]:数据头，固定为0x66 BYTE[1]:高四位是炮台左右；左或者右的标识在BYTE3.1，左右各15档；
 * 低四位是炮台上下及打炮，标识在BYTE3.0，各15档； BYTE[2]:高四位是左右，标识BYTE3.6; 低四位是前后；标识BYTE3.5;
 * BYTE[3]:BIT0：炮台左右标识，左是1；右是0； BIT1：炮台上下标识，左是1，右是0； BIT2：打炮按键，有按时为1；没按时为0；
 * BIT3: 机关枪按键，有按时为1；没按时为0； BIT4：开关键按键，有按时为1；没按时为0； BIT5：左右标识，左是1；右是0；
 * BIT6：前后标识，前是1；后是0；
 * BYTE[4]:校验字节=字节1+字节2+字节3+0x0B;如果数据中断：前三个字节发送0x0，第四个字节发送0x0C;
 * BYTE[5]：数据尾，固定为0x99；
 * 
 * 
 */
public class ControlMsg {

	private static final int CONNON_FOUR_HIGH_MIN_R = 15;
	private static final int CONNON_FOUR_HIGH_MIN_L = 15;
	private static final int RIGHT_LEFT_MIN_R = 9;
	private static final int RIGHT_LEFT_MIN_L = 13;

	private static ControlMsg sControlMsg;
	private byte[] data = new byte[6];

	// 0
	private static final byte head = (byte) 0x66;// MSG 头
	// 1
	private byte connon_four;
	private byte connon_four_high;// 高四位是炮台左右
	private byte connon_four_low;// 低四位是炮台上下及打炮
	// 2
	private byte four;
	private byte four_high;// 高四位
	private byte four_low;// 低四位
	// 3
	// 3.1
	private byte connon_up_down;// 1 左 0右
	// 3.2
	private byte connon_left_right;// 1 左 0右
	// 3.3
	private byte connon_btn;// 按时为1；没按时为0
	// 3.4
	private byte gun_btn;// 有按时为1；没按时为0；
	// 3.5
	private byte power_btn;// 有按时为1；没按时为0；
	// 3.6
	private byte front_back;// 前1 后0
	// 3.7
	private byte left_right;// 左1 右0
	// 4
	private byte checksum; // 校验位BYTE[0]+BYTE[1]+BYTE[2]+0x0B;
	// 5
	private static final byte tail = (byte) 0x99;// MSG 尾

	/**
	 * 获取ControlMsg的一个对象
	 * 
	 * @return
	 */
	public static ControlMsg getInstance() {
		synchronized (ControlMsg.class) {
			if (sControlMsg == null) {
				sControlMsg = new ControlMsg();
			}
			return sControlMsg;
		}
	}

	public ControlMsg() {
		data[0] = head;
		connon_four = 0;
		four = 0;
		connon_up_down = 1;
		connon_left_right = 1;
		connon_btn = 0;
		gun_btn = 0;
		power_btn = 0;

		front_back = 1;
		left_right = 0;

	}

	public void setData() {
		data[0] = head;
		data[1] = connon_four;
		data[2] = four;
		/*
		 * data[3] = (byte) (connon_left_right | connon_up_down | connon_btn |
		 * gun_btn | power_btn | left_right | front_back);
		 */
		data[3] = (byte) (connon_up_down * 1 + connon_left_right * 2
				+ connon_btn * 4 + gun_btn * 8 + power_btn * 16 + front_back
				* 32 + left_right * 64);
		data[4] = (byte) (data[1] + data[2] + data[3] + 0x0B);
		data[5] = tail;
	}

	/**
	 * @return data数组
	 */
	public byte[] getData() {
		setData();
		return data;
	}

	/**
	 * @param index
	 *            第index个元素
	 * @return data[index]的HEX字符串
	 */
	public String getHexData(int index) {
		return String.format("%02x", data[index]);
	}

	/**
	 * 将data数组转换成HEX字符串
	 * 
	 * @param data间分割符
	 * @return
	 */
	public String getDataHexString(String separator) {
		setData();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			sb.append(getHexData(i));
			if (i < data.length - 1) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	public int setConnonFour(float x, float y) {

		connon_four = (byte) ((byte) (x * 16 + y) & 0xff);

		return connon_four;
	}

	public int setFour(float x, float y) {

		four = (byte) ((byte) (x * 16 + y) & 0xff);

		return four;
	}

	int tmp1;

	// 1左右打炮档1-15
	public int setConnonFourLow(float f) {

		int tmp = (int) ((f * CONNON_FOUR_HIGH_MIN_L));

		if (f < 0) {
			tmp1 = -tmp;
		} else {
			tmp1 = tmp;
		}

		return tmp1;
	}

	public int setConnonFourLow_x(float f) {

		int tmp = (int) ((f * RIGHT_LEFT_MIN_L));

		if (f < 0) {
			tmp1 = -tmp;
		} else {
			tmp1 = tmp;
		}

		return tmp1;
	}

	public int getConnonFourLow() {
		return connon_four_low & 0xff;
	}

	// 2 前后左右
	int tmp3;

	public int setFourLow(float f) {
		int tmp = (int) ((f * CONNON_FOUR_HIGH_MIN_R));
		if (f < 0) {
			tmp3 = -tmp;
		} else {
			tmp3 = tmp;
		}

		return tmp3;
	}

	public int setFourLow_x(float f) {
		int tmp = (int) ((f * RIGHT_LEFT_MIN_R));
		if (f < 0) {
			tmp3 = -tmp;
		} else {
			tmp3 = tmp;
		}

		return tmp3;
	}

	public int getFourLow() {
		return four_low & 0xff;
	}

	// 3.0
	public void setConnonLeftRight(int connon) {
		this.connon_left_right = (byte) (connon);
	}

	public int getConnonLeftRight() {
		return (connon_left_right & 0xff);
	}

	// 3.1
	public void setConnonUpDown(int upDown) {
		this.connon_up_down = (byte) (upDown);
	}

	public int getConnonUpDown() {
		return (connon_up_down & 0xff);
	}

	// 3.2
	public void setConnonBtn(int con) {
		this.connon_btn = (byte) (con);
	}

	public int getConnonBtn() {
		return (connon_btn & 0xff);
	}

	// 3.3
	public void setGunbtn(int gun) {
		this.gun_btn = (byte) (gun);
	}

	public int getGunBtn() {
		return (gun_btn & 0xff);
	}

	// 3.4
	public void setPowerBtn(int power) {
		this.power_btn = (byte) (power);
	}

	public int getPowerBtn() {
		return (power_btn & 0xff);
	}

	// 3.5
	public void setLeftRight(int leftRight) {
		this.left_right = (byte) (leftRight);
	}

	public int getLeftRight() {
		return (left_right & 0xff);
	}

	// 3.6
	public void setFrontBack(int frontBack) {
		this.front_back = (byte) (frontBack);
	}

	public int getFrontBack() {
		return (front_back & 0xff);
	}

}
