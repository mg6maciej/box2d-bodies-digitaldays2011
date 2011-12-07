package pl.mg6.digitaldays2011.box2d.bodies;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class BodiesExampleView extends View {
	
	private static final String TAG = BodiesExampleView.class.getSimpleName();
	
	private BodiesExampleModel model;
	
	private final Paint paint;
	
	public static final float VIEWPORT_SIZE = 20.0f; // meters
	
	public BodiesExampleView(Context context) {
		super(context);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(0.05f);
	}

	public void setModel(BodiesExampleModel model) {
		this.model = model;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(0.0f, getHeight());
		float scale = getWidth() / VIEWPORT_SIZE;
		canvas.scale(scale, -scale);
		drawBodies(canvas);
	}
	
	private void drawBodies(Canvas canvas) {
		Body body = model.getBodyList();
		while (body != null) {
			Fixture fixture = body.getFixtureList();
			while (fixture != null) {
				Shape shape = fixture.getShape();
				drawShape(canvas, body.getPosition(), body.getAngle(), shape);
				fixture = fixture.getNext();
			}
			body = body.getNext();
		}
	}
	
	private void drawShape(Canvas canvas, Vec2 pos, float angle, Shape shape) {
		canvas.save();
		canvas.rotate(180.0f * angle / MathUtils.PI, pos.x, pos.y);
		if (shape.m_type == ShapeType.CIRCLE) {
			CircleShape circle = (CircleShape) shape;
			paint.setColor(0xFFFF6666);
			canvas.drawCircle(pos.x + circle.m_p.x, pos.y + circle.m_p.y, circle.m_radius, paint);
			paint.setColor(0xFFFFFFFF);
			canvas.drawLine(pos.x + circle.m_p.x, pos.y + circle.m_p.y, pos.x + circle.m_p.x + circle.m_radius, pos.y + circle.m_p.y, paint);
		} else if (shape.m_type == ShapeType.POLYGON) {
			PolygonShape polygon = (PolygonShape) shape;
			Path path = new Path();
			Vec2 vec = polygon.m_vertices[polygon.m_vertexCount - 1];
			path.moveTo(pos.x + vec.x, pos.y + vec.y);
			for (int i = 0; i < polygon.m_vertexCount; i++) {
				vec = polygon.m_vertices[i];
				path.lineTo(pos.x + vec.x, pos.y + vec.y);
			}
			if (polygon.m_vertexCount == 3) {
				paint.setColor(0xFF66FF66);
			} else {
				paint.setColor(0xFF6666FF);
			}
			canvas.drawPath(path, paint);
		}
		canvas.restore();
	}
}
