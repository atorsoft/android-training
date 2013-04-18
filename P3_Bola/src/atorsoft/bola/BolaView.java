package atorsoft.bola;

import java.util.Random;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class BolaView extends SurfaceView implements SurfaceHolder.Callback {

	// Radio
	private static final int RADIO = 30;

	// Imagen
	private Bitmap mImagen;

	// Posición (punto central)
	private PointF mPosicion;

	// Dimensiones del área de movimiento
	private int mAnchura;
	private int mAltura;

	// Límite máximo de coordenadas (el mínimo es RADIO)
	private int mLimiteMaxX;
	private int mLimiteMaxY;

	// Estado
	private boolean mEnMovimiento = false;

	// Movimiento
	private MovimientoRectilineo mMovimiento;

	// Tolerancia de mínimo ángulo de movimiento (en grados)
	private static final int MIN_ANGULO_MOVIMIENTO = 15;

	// Distancia en cada paso de movimiento
	private int mDistanciaPaso = 1;

	// Velocidad
	private int mVelocidad = 1;
	private static final int VELOCIDAD_MAXIMA = 15;

	// Incremento de distancia en cada aumento de velocidad
	private static final int DISTANCIA_INCREMENTO = 1;

	// Aleatoriedad
	private static final Random RANDOM = new Random();

	// Thread
	private BolaThread mThread = null;

	public BolaView(Context contexto) {
		super(contexto);

		// Asignación de callback a la superficie subyacente del SurfaceView
		getHolder().addCallback(this);

		// Creación de thread
		mThread = new BolaThread(getHolder(), this);

		// Inicialización de parámetros
		Display display = ((WindowManager) contexto
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		mAnchura = display.getWidth();
		mAltura = display.getHeight();
		mLimiteMaxX = mAnchura - RADIO;
		mLimiteMaxY = mAltura - RADIO;

		Bitmap ic_launcher = BitmapFactory.decodeResource(
				contexto.getResources(), R.drawable.ic_launcher);
		mImagen = Bitmap.createScaledBitmap(ic_launcher, 2 * RADIO, 2 * RADIO,
				true);

		mPosicion = new PointF(mAnchura / 2, mAltura / 2);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mThread.setEjecucion(true);
		mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mThread.setEjecucion(false);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xFFAAAAAA);
		canvas.drawBitmap(mImagen, mPosicion.x - RADIO, mAltura - mPosicion.y
				- RADIO, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			PointF p = new PointF(event.getX(), event.getY());
			RectF rect = new RectF(mPosicion.x - RADIO, mAltura - mPosicion.y
					- RADIO, mPosicion.x + RADIO, mAltura - mPosicion.y + RADIO);
			if (p.x >= rect.left && p.x <= rect.right && p.y >= rect.top
					&& p.y <= rect.bottom) {
				setEnMovimiento(false);
			} else {
				if (!isEnMovimiento()) {
					if (mMovimiento == null) {
						obtenerMovimientoAleatorio();
					}
					setEnMovimiento(true);
				} else {
					acelerar();
				}
			}
		}
		return true;
	}

	private void obtenerMovimientoAleatorio() {
		PointF pDestino = new PointF();
		do {
			pDestino.x = RADIO + ((mLimiteMaxX - RADIO) * RANDOM.nextFloat());
			pDestino.y = RADIO + ((mLimiteMaxY - RADIO) * RANDOM.nextFloat());
			mMovimiento = new MovimientoRectilineo(mPosicion, pDestino);
		} while ((pDestino.equals(mPosicion.x, mPosicion.y))
				|| (mMovimiento.calcularAnguloAbsolutoGrados() < MIN_ANGULO_MOVIMIENTO));
	}

	public void moverPosicion() {
		PointF nuevaPosicion = new PointF();
		nuevaPosicion.x = mPosicion.x
				+ mMovimiento.getDistanciaX(mDistanciaPaso);
		nuevaPosicion.y = mPosicion.y
				+ mMovimiento.getDistanciaY(mDistanciaPaso);
		comprobarRebote(nuevaPosicion);
	}

	private void comprobarRebote(PointF nuevaPosicion) {
		boolean rebote = false;

		if (nuevaPosicion.x <= RADIO || nuevaPosicion.x >= mLimiteMaxX
				|| nuevaPosicion.y <= RADIO || nuevaPosicion.y >= mLimiteMaxY) {

			rebote = true;

			if (nuevaPosicion.x < RADIO || nuevaPosicion.x > mLimiteMaxX) {
				if (nuevaPosicion.x < RADIO) {
					nuevaPosicion.x = RADIO;
				} else if (nuevaPosicion.x > mLimiteMaxX) {
					nuevaPosicion.x = mLimiteMaxX;
				}
				nuevaPosicion.y = mMovimiento.calcularY(nuevaPosicion.x);
			}

			if (nuevaPosicion.y < RADIO || nuevaPosicion.y > mLimiteMaxY) {
				if (nuevaPosicion.y < RADIO) {
					nuevaPosicion.y = RADIO;
				} else if (nuevaPosicion.y > mLimiteMaxY) {
					nuevaPosicion.y = mLimiteMaxY;
				}
				nuevaPosicion.x = mMovimiento.calcularX(nuevaPosicion.y);
			}
		}
		mPosicion = nuevaPosicion;

		if (rebote) {
			obtenerMovimientoAleatorio();
		}
	}

	private void acelerar() {
		if (mVelocidad <= VELOCIDAD_MAXIMA) {
			mDistanciaPaso += DISTANCIA_INCREMENTO;
			mVelocidad++;
		}
	}

	public BolaThread getThread() {
		return mThread;
	}

	public boolean isEnMovimiento() {
		return mEnMovimiento;
	}

	private void setEnMovimiento(boolean enMovimiento) {
		this.mEnMovimiento = enMovimiento;
	}

}