/**
 * 
 */
package obiee.udmlparser.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * @author danielgalassi@gmail.com
 *
 */
public class Request {

	private Map<String, String> request = new HashMap<String, String>();
	private boolean isBusMatrixInvoked = false;
	private boolean isTransformationInvoked = false;

	private void invokeTransformation(boolean invoked) {
		isTransformationInvoked = invoked;
	}

	public boolean isTransformationInvoked() {
		return isTransformationInvoked;
	}

	public void invokeBusMatrix(String value) {
		isBusMatrixInvoked = (value.equals("busmatrix"));
	}

	public boolean isBusMatrixInvoked() {
		return isBusMatrixInvoked;
	}

	public void setArg(String key, String value) {
		request.put(key, value);
		invokeTransformation(true);
	}

	public String getArg(String key) {
		String value = "";
		if (request.containsKey(key)) {
			value = request.get(key);
		}
		return value;
	}

	public void changeArgTo(String key, String value) {
		if (request.containsKey(key)) {
			request.remove(key);
			setArg(key, value);
		}
	}
}
