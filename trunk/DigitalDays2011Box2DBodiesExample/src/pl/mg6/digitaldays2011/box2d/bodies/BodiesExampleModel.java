package pl.mg6.digitaldays2011.box2d.bodies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import android.util.Log;

public class BodiesExampleModel implements ContactListener {
	
	private static final String TAG = BodiesExampleModel.class.getSimpleName();

	private World world;
	private Body groundBody;
	
	private long timeAccumulator;
	
	private static final long stepInMillis = 20;
	private static final float stepInSeconds = stepInMillis / 1000.0f;
	private static final int velocityIterations = 10;
	private static final int positionIterations = 5;
	
	private List<MouseJoint> userActions = new ArrayList<MouseJoint>();
	
	private static final int DESTROY_ON_TOUCH = 1;
	private static final int APPLY_FORCE_ON_TOUCH = 2;
	private static final int DRAG_ON_TOUCH = 3;
	
	private Body kinematicBody;
	private Body sensorBody;
	
	private boolean createNewBall;
	
	public BodiesExampleModel() {
		init();
	}
	
	public void beginContact(Contact contact) {
		// do not try to create/destroy bodies during callbacks (in the middle of world.step)
		if (contact.getFixtureA().getBody() == sensorBody || contact.getFixtureB().getBody() == sensorBody) {
			Object data = contact.getFixtureA().m_userData;
			if (data == null) {
				data = contact.getFixtureB().m_userData;
			}
			if (data == null) {
				data = contact.getFixtureA().getBody().m_userData;
			}
			if (data == null) {
				data = contact.getFixtureB().getBody().m_userData;
			}
			if (data instanceof Integer && DRAG_ON_TOUCH == (Integer) data) {
				createNewBall = true;
			}
		}
	}
	
	public void endContact(Contact contact) {
	}
	
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
	
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}
	
	private void init() {
		Vec2 gravity = new Vec2(0.0f, -10.0f);
		boolean doSleep = true;
		world = new World(gravity, doSleep);
		
		world.setContactListener(this);
		
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(10.0f, 0.0f);
		groundBody = world.createBody(groundBodyDef);
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(11.0f, 1.0f, new Vec2(0.0f, -1.0f), 0.0f);
		groundBody.createFixture(polygonShape, 1.0f);
		polygonShape.setAsBox(1.0f, 17.0f, new Vec2(-11.0f, 16.0f), 0.0f);
		groundBody.createFixture(polygonShape, 1.0f);
		polygonShape.setAsBox(1.0f, 17.0f, new Vec2(11.0f, 16.0f), 0.0f);
		groundBody.createFixture(polygonShape, 1.0f);
		polygonShape.setAsBox(11.0f, 1.0f, new Vec2(0.0f, 33.0f), 0.0f);
		groundBody.createFixture(polygonShape, 1.0f);
		
		FixtureDef fixtureDef;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		Body body;
		
		// bouncing circles
		bodyDef.position.set(1.0f, 6.0f);
		body = world.createBody(bodyDef);
		float restitution = 1.0f;
		fixtureDef = createCircleFixtureDef(1.0f, 0.1f, restitution, 0.0f, 0.0f, 0.4f);
		fixtureDef.userData = DRAG_ON_TOUCH;
		body.createFixture(fixtureDef);

		bodyDef.position.set(2.0f, 6.0f);
		body = world.createBody(bodyDef);
		restitution = 0.8f;
		fixtureDef.restitution = restitution;
		fixtureDef.userData = DESTROY_ON_TOUCH;
		body.createFixture(fixtureDef);

		bodyDef.position.set(3.0f, 6.0f);
		body = world.createBody(bodyDef);
		restitution = 0.0f;
		fixtureDef.restitution = restitution;
		fixtureDef.userData = APPLY_FORCE_ON_TOUCH;
		body.createFixture(fixtureDef);
		
		// rects with friction
		bodyDef.position.set(4.0f, 6.0f);
		body = world.createBody(bodyDef);
		float friction = 0.5f;
		fixtureDef = createRectFixtureDef(1.0f, friction, 0.1f, 0.0f, 0.0f, 0.8f, 0.8f, 0.0f);
		fixtureDef.userData = DESTROY_ON_TOUCH;
		body.createFixture(fixtureDef);

		bodyDef.position.set(5.0f, 6.0f);
		body = world.createBody(bodyDef);
		friction = 0.03f;
		fixtureDef.friction = friction;
		fixtureDef.userData = APPLY_FORCE_ON_TOUCH;
		body.createFixture(fixtureDef);

		bodyDef.position.set(6.0f, 6.0f);
		body = world.createBody(bodyDef);
		friction = 0.001f;
		fixtureDef.friction = friction;
		fixtureDef.userData = DRAG_ON_TOUCH;
		body.createFixture(fixtureDef);
		
		// ground for above rects
		bodyDef.position.set(6.0f, 2.0f);
		bodyDef.type = BodyType.STATIC;
		body = world.createBody(bodyDef);
		fixtureDef = createRectFixtureDef(1.0f, 0.2f, 0.0f, 0.0f, 0.0f, 5.0f, 0.2f, -0.1f);
		body.createFixture(fixtureDef);
		
		// ground for circles
		bodyDef.position.set(13.0f, 2.0f);
		body = world.createBody(bodyDef);
		fixtureDef = createRectFixtureDef(1.0f, 0.2f, 0.0f, 0.0f, 0.0f, 3.0f, 0.2f, 0.0f);
		body.createFixture(fixtureDef);
		
		// circles with different mass (density)
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(12.0f, 4.0f);
		body = world.createBody(bodyDef);
		float density = 1.0f;
		fixtureDef = createCircleFixtureDef(density, 0.1f, 0.4f, 0.0f, 0.0f, 0.4f);
		fixtureDef.userData = APPLY_FORCE_ON_TOUCH;
		body.createFixture(fixtureDef);

		bodyDef.position.set(13.0f, 4.0f);
		body = world.createBody(bodyDef);
		density = 2.0f;
		fixtureDef.density = density;
		body.createFixture(fixtureDef);

		bodyDef.position.set(14.0f, 4.0f);
		body = world.createBody(bodyDef);
		density = 5.0f;
		fixtureDef.density = density;
		body.createFixture(fixtureDef);
		
		// kinematic object
		bodyDef.type = BodyType.KINEMATIC;
		bodyDef.position.set(10.0f, 6.0f);
		kinematicBody = world.createBody(bodyDef);
		fixtureDef = createRectFixtureDef(1.0f, 0.2f, 0.0f, 0.0f, 0.0f, 3.0f, 0.5f, 0.0f);
		kinematicBody.createFixture(fixtureDef);
		kinematicBody.setLinearVelocity(new Vec2(2.0f, 0.0f));
		
		// sensor
		bodyDef.type = BodyType.STATIC;
		bodyDef.position.set(10.0f, 10.0f);
		sensorBody = world.createBody(bodyDef);
		fixtureDef = createRectFixtureDef(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 0.0f);
		fixtureDef.isSensor = true;
		sensorBody.createFixture(fixtureDef);
		
		// complex body
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(17.0f, 8.0f);
		bodyDef.userData = DRAG_ON_TOUCH;
		body = world.createBody(bodyDef);
		fixtureDef = createRectFixtureDef(1.0f, 0.2f, 0.3f, 0.0f, 0.0f, 3.0f, 0.3f, -1.0f);
		body.createFixture(fixtureDef);
		fixtureDef = createRectFixtureDef(1.0f, 0.2f, 0.3f, 0.0f, 0.0f, 3.0f, 0.3f, 1.0f);
		body.createFixture(fixtureDef);
		fixtureDef = createCircleFixtureDef(1.0f, 0.2f, 0.2f, 2.0f, 0.0f, 1.0f);
		body.createFixture(fixtureDef);
	}
	
	private FixtureDef createFixtureDef(float density, float friction, float restitution) {
		FixtureDef def = new FixtureDef();
		def.density = density;
		def.friction = friction;
		def.restitution = restitution;
		return def;
	}
	
	private FixtureDef createCircleFixtureDef(float density, float friction, float restitution, float x, float y, float radius) {
		FixtureDef def = createFixtureDef(density, friction, restitution);
		CircleShape shape = new CircleShape();
		shape.m_radius = radius;
		shape.m_p.set(x, y);
		def.shape = shape;
		return def;
	}
	
	private FixtureDef createRectFixtureDef(float density, float friction, float restitution, float x, float y, float w, float h, float angle) {
		FixtureDef def = createFixtureDef(density, friction, restitution);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(w / 2.0f, h / 2.0f, new Vec2(x, y), angle);
		def.shape = shape;
		return def;
	}
	
	public void update(long dt) {
		// kinamatic body velocity update
		if (kinematicBody.getPosition().x >= 20.0f) {
			kinematicBody.setLinearVelocity(new Vec2(-2.0f, 0.0f));
			kinematicBody.setAngularVelocity(0.0f);
		}
		if (kinematicBody.getPosition().x <= 0.0f) {
			kinematicBody.setLinearVelocity(new Vec2(2.0f, 0.0f));
			kinematicBody.setAngularVelocity(0.5f);
		}
		timeAccumulator += dt;
		//int stepsDuringUpdate = 0;
		while (timeAccumulator >= stepInMillis) {
			world.step(stepInSeconds, velocityIterations, positionIterations);
			timeAccumulator -= stepInMillis;
			//stepsDuringUpdate++;
		}
		if (createNewBall) {
			BodyDef def = new BodyDef();
			def.type = BodyType.DYNAMIC;
			def.position.set(10.0f, 10.0f);
			def.userData = DESTROY_ON_TOUCH;
			Body body = world.createBody(def);
			FixtureDef fixtureDef = createCircleFixtureDef(1.0f, 0.2f, 0.6f, 0.0f, 0.0f, 0.5f);
			body.createFixture(fixtureDef);
			createNewBall = false;
		}
		//Log.i(TAG, "steps during update: " + stepsDuringUpdate);
	}

	public Body getBodyList() {
		return world.getBodyList();
	}

	public void userActionStart(int pointerId, final float x, final float y) {
		final List<Fixture> fixtures = new ArrayList<Fixture>();
		final Vec2 vec = new Vec2(x, y);
		world.queryAABB(new QueryCallback() {
			public boolean reportFixture(Fixture fixture) {
				Log.i(TAG, "reportFixture: " + fixture);
				//if (fixture.testPoint(vec)) {
					fixtures.add(fixture);
				//}
				return true;
			}
		}, new AABB(vec, vec));
		if (fixtures.size() > 0) {
			Fixture fixture = fixtures.get(0);
			Body body = fixture.getBody();
			
			Object data = fixture.getUserData();
			if (data == null) {
				data = body.getUserData();
			}
			if (data instanceof Integer) {
				int intData = ((Integer) data).intValue();
				if (DESTROY_ON_TOUCH == intData) {
					if (body.m_fixtureCount > 1) {
						body.destroyFixture(fixture);
					} else {
						world.destroyBody(body);
					}
				} else if (APPLY_FORCE_ON_TOUCH == intData) {
					Random r = new Random();
					body.applyForce(new Vec2(100.0f, 300.0f), body.getWorldCenter());
				} else if (DRAG_ON_TOUCH == intData) {
					MouseJointDef def = new MouseJointDef();
					def.bodyA = body;
					def.bodyB = body;
					def.maxForce = 1000.0f * body.getMass();
					def.target.set(x, y);
					
					MouseJoint joint = (MouseJoint) world.createJoint(def);
					joint.m_userData = pointerId;
					userActions.add(joint);
				}
			}
		}
	}

	public void userActionUpdate(int pointerId, float x, float y) {
		for (MouseJoint joint : userActions) {
			if (pointerId == (Integer) joint.m_userData) {
				joint.setTarget(new Vec2(x, y));
				break;
			}
		}
	}

	public void userActionEnd(int pointerId, float x, float y) {
		for (MouseJoint joint : userActions) {
			if (pointerId == (Integer) joint.m_userData) {
				world.destroyJoint(joint);
				userActions.remove(joint);
				break;
			}
		}
	}
}
