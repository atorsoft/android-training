package atorsoft.bola;

import android.graphics.PointF;

public class MovimientoRectilineo {

	private PointF mP1, mP2;
	private double mPendiente;
	private double mAngulo;

	public MovimientoRectilineo(PointF p1, PointF p2) {
		mP1 = p1;
		mP2 = p2;
		mPendiente = (mP2.y - mP1.y) / (mP2.x - mP1.x);
		mAngulo = Math.atan(mPendiente);
	}

	public float getDistanciaX(int distancia) {
		float mDistanciaX = (Double.valueOf(distancia * Math.cos(mAngulo)))
				.floatValue();
		if (mP1.x > mP2.x) {
			return -mDistanciaX;
		} else {
			return mDistanciaX;
		}
	}

	public float getDistanciaY(int distancia) {
		float mDistanciaY = (Double.valueOf(distancia * Math.sin(mAngulo)))
				.floatValue();
		if (mP1.x > mP2.x) {
			return -mDistanciaY;
		} else {
			return mDistanciaY;
		}
	}

	public float calcularX(float y) {
		double x = mP1.x + ((y - mP1.y) / mPendiente);
		return (Double.valueOf(x)).floatValue();
	}

	public float calcularY(float x) {
		double y = mP1.y + ((x - mP1.x) * mPendiente);
		return (Double.valueOf(y)).floatValue();
	}

	public double calcularAnguloAbsolutoGrados() {
		return Math.abs(Math.toDegrees(mAngulo));
	}

}
