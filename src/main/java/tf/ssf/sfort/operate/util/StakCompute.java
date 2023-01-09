package tf.ssf.sfort.operate.util;

public interface StakCompute {
	boolean computeAdd();
	boolean computeSub();
	boolean computeGreater();
	boolean computeLesser();
	boolean computeDiv();
	boolean computeMul();
	boolean computeAnd();
	boolean computeXor();
	boolean computeNot();
	boolean computeEquals();
	boolean computeDup();
	boolean computePop();
	boolean computeTick();
	boolean computeIf0();
	boolean computeSwap();
	boolean computeShiftLeft();
	boolean computeShiftRight();
	boolean computeMark();
	boolean computeJump();
	boolean computeGetColorStrength();
	boolean computeStore();
	boolean computeLoad();
	boolean computeColorLoad();
	boolean computeColorAdd();
	boolean computeColorSubtract();
	boolean computeConst(int con);
}
