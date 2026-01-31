
package actions;

import backpack.object.Curse;
import java.util.Objects;


public class Action {
	private final ActionType typeAction;
	private final int val;
	private final Curse curse;

	public Action(ActionType typeAction, int val, Curse curse) {
		Objects.requireNonNull(typeAction);
		this.typeAction = typeAction;
		this.val = val;
		this.curse = curse;
	}

	public ActionType getType() {
		return typeAction;
	}

	public int getVal() {
		return val;
	}

	public Curse getGear() {
		return curse;
	}

}
