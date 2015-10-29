package jp.ac.u_tokyo.slamwithcameraimu;

import java.util.ArrayList;
import java.util.Collections;


public class Utils {

	/**
	 * High-pass filter
	 * @param newValues 新しい値
	 * @param lowValue 前回の低周波領域値が渡され、今回の低周波領域値が格納される配列
	 * @param value ハイパスフィルタ適用後の値が格納される配列
	 */
	static void highPassFilter(float[] newValues, float[] lowValue, float[] value, float alpha) {
	    lowValue[0] = alpha * lowValue[0] + (1 - alpha) * newValues[0];
	    lowValue[1] = alpha * lowValue[1] + (1 - alpha) * newValues[1];
	    lowValue[2] = alpha * lowValue[2] + (1 - alpha) * newValues[2];
	    value[0] = newValues[0] - lowValue[0];
	    value[1] = newValues[1] - lowValue[1];
	    value[2] = newValues[2] - lowValue[2];
	}

	/*
	 * Low-pass filter
	 */
	static float lowPassFilterSingle(float values, float newValues, float alpha){
		return (alpha * values + (1 - alpha) * newValues);
	}

	/*
	 * Low-pass filter
	 */
	static void lowPassFilter(float[] values, float[] newValues, float alpha){
		//final float alpha = 0.8f;
		values[0] = alpha * values[0] + (1 - alpha) * newValues[0];
		values[1] = alpha * values[1] + (1 - alpha) * newValues[1];
		values[2] = alpha * values[2] + (1 - alpha) * newValues[2];
	}

	/*
	 * Median filter
	 */
	static void medianFilter(float[] values, ArrayList<Float> valueX, ArrayList<Float> valueY, ArrayList<Float> valueZ, int medianNum){
		//X
		ArrayList<Float> lst = (ArrayList<Float>) valueX.clone();
		Collections.sort(lst);
		values[0] = lst.get(medianNum);
		//Y
		lst = (ArrayList<Float>) valueY.clone();
		Collections.sort(lst);
		values[1] = lst.get(medianNum);
		//Z
		lst = (ArrayList<Float>) valueZ.clone();
		Collections.sort(lst);
		values[2] = lst.get(medianNum);
	}

	/*
	 * Median filter + Low-pass filter
	 */
	static void medianLPFilter(float[] values, ArrayList<Float> valueX, ArrayList<Float> valueY, ArrayList<Float> valueZ, int medianNum, float alpha){
		//X
		ArrayList<Float> lst = (ArrayList<Float>) valueX.clone();
		Collections.sort(lst);
		values[0] = (values[0]*alpha) + lst.get(medianNum)*(1 - alpha);
		//Y
		lst = (ArrayList<Float>) valueY.clone();
		Collections.sort(lst);
		values[1] = (values[1]*alpha) + lst.get(medianNum)*(1 - alpha);
		//Z
		lst = (ArrayList<Float>) valueZ.clone();
		Collections.sort(lst);
		values[2] = (values[2]*alpha) + lst.get(medianNum)*(1 - alpha);
	}

	/*
	 * Calc orientation from Gravity
	 * see also "Studies on Orientation Measurement in Sports Using Inertial and Magnetic Field Sensors"
	 * https://www.jstage.jst.go.jp/article/sposun/22/2/22_255/_pdf
	 */
	static void calcOrientationFromGravity(float[] gravity, float[] magnet, float[] orientation){
		/// 重力の値は正負を入れ替えて使用することに注意 ///
		/// X軸
		orientation[0] = (float) Math.atan2(-gravity[1],-gravity[2]);
		/// Y軸 （-90～+90までしか表現できない）
		orientation[1] = (float) Math.atan2(gravity[0],Math.hypot(-gravity[1],-gravity[2]));
		/// Z軸
		float magnet_x_fixed = (float) (Math.cos(orientation[1])*magnet[0] + Math.sin(orientation[0])*Math.sin(orientation[1])*magnet[1] + Math.cos(orientation[0])*Math.sin(orientation[1])*magnet[2]);
		float magnet_y_fixed = (float) (Math.cos(orientation[0])*magnet[1] - Math.sin(orientation[0])*magnet[2]);
		orientation[2] = (float) Math.atan2(-magnet_y_fixed,magnet_x_fixed);
	}

	/*
	 * Remove bias of acceleration
	 * 加速度の系統誤差を取り除く
	 */
	static void removeAccelBias(float[] acceleration, float[] newValue, float[] gravity, float[] orientation){
		/// Y軸
		if(gravity[1] > 0){ //Y軸が上を向いているとき
			acceleration[1] = (float) (newValue[1] - 0.4f * Math.sin(3.0f*(-orientation[0])));
		}else{ //Y軸が下を向いているとき
			acceleration[1] = (float) (newValue[1] + 1.2f * Math.sin(orientation[0]));
		}
		/// Z軸
		if(gravity[2] > 0){ //Z軸が上を向いているとき
			acceleration[2] = (float) (newValue[2] - 0.4f * Math.sin(3.0f*(orientation[0] - Math.PI/2.0f)) * Math.sin(3.0f*(orientation[1] - Math.PI/2.0f)));
		}else{ //Z軸が下を向いているとき
			acceleration[2] = (float) (newValue[2] + 1.2f * Math.sin(orientation[0] + Math.PI/2.0f) * Math.sin(orientation[1] + Math.PI/2.0f));
		}
	}
}
