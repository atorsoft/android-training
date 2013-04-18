package atorsoft.bola;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class BolaThread extends Thread {

	private SurfaceHolder surfaceHolder;
	private BolaView bolaView;

	private boolean ejecucion = false;

	public BolaThread(SurfaceHolder surfaceHolder, BolaView bolaView) {
		this.surfaceHolder = surfaceHolder;
		this.bolaView = bolaView;
	}

	public void setEjecucion(boolean valor) {
		this.ejecucion = valor;
	}

	@Override
	public void run() {
		Canvas c;
		while (ejecucion) {
			c = null;
			try {
				c = surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					if (bolaView.isEnMovimiento())
						bolaView.moverPosicion();
					bolaView.onDraw(c);
				}
			} finally {
				// Hacer esto en un bloque finally
				// Si ocurre un error, no se deja el SurfaceView
				// inconsistente
				if (c != null) {
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}

}