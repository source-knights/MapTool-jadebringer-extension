/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.frameworks.base.functions;

import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenPropertyFunctions;
import net.rptools.maptool.client.functions.frameworkfunctions.ExtensionFunction;
import net.rptools.maptool.client.functions.frameworkfunctions.FrameworksFunctions.Run;
import net.rptools.maptool.client.functions.frameworkfunctions.FunctionCaller;
import net.rptools.maptool.client.functions.frameworkfunctions.FunctionCaller.TokenWrapper;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

/**
 * 
 * @author oliver.szymanski
 */
public class SettingsFunctions extends ExtensionFunction {
	public static final String LIB_JADEBRINGER_SETTINGS = "Lib:JadebringerSettings";
	public static final String LIST_SETTINGS = "listSettings";
	public static final String DELETE_SETTING = "deleteSetting";
	public static final String GET_SETTING = "getSetting";
	public static final String SET_SETTING = "setSetting";

	public SettingsFunctions() {
		super( 
		    Alias.create(SET_SETTING, 2, 3), 
		    Alias.create(GET_SETTING, 1 ,3), 
		    Alias.create(DELETE_SETTING, 1, 1), 
		    Alias.create(LIST_SETTINGS, 0, 0));
		setTrustedRequired(true);
	}

	private final static SettingsFunctions instance = new SettingsFunctions();

	public static SettingsFunctions getInstance() {
		return instance;
	}

	@Override
	public Object run(Parser parser, String functionName, List<Object> parameters) throws ParserException {

		if (SET_SETTING.equals(functionName)) {
			String key = FunctionCaller.getParam(parameters, 0);
			Object value = FunctionCaller.getParam(parameters, 1);
			String tokenName = FunctionCaller.getParam(parameters, 2,LIB_JADEBRINGER_SETTINGS);
			return setSetting(parser, key, value, tokenName);
		} else if (GET_SETTING.equals(functionName)) {
			String key = FunctionCaller.getParam(parameters, 0);
			Object defaultValue = FunctionCaller.getParam(parameters, 1);
			String tokenName = FunctionCaller.getParam(parameters, 2,LIB_JADEBRINGER_SETTINGS);
			return getSetting(parser, key, defaultValue, tokenName);
		} else if (DELETE_SETTING.equals(functionName)) {
			String key = FunctionCaller.getParam(parameters, 0);
			String tokenName = FunctionCaller.getParam(parameters, 1,LIB_JADEBRINGER_SETTINGS);
			deleteSetting(parser, key, tokenName);
			return key + " deleted";
		} else if (LIST_SETTINGS.equals(functionName)) {
			String tokenName = FunctionCaller.getParam(parameters, 0,LIB_JADEBRINGER_SETTINGS);
			return listSettings(parser, tokenName);
		}
	
		return throwNotFoundParserException(functionName);
	}

	public String setSetting(Parser parser, String key, Object value, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		if (tokenName == null) { tokenName = LIB_JADEBRINGER_SETTINGS; }
    
		if (tokenName.toLowerCase().startsWith("lib:")) {
		  FunctionCaller.callFunction("setLibProperty", tpFunc, parser, key, value, tokenName);
		} else {
		  TokenWrapper token = FunctionCaller.findToken(tokenName, null, true);
		  try {
		  FunctionCaller.doWithToken(parser, token, new Run<Object>() {
        public Object run() throws Exception {
          return FunctionCaller.callFunction("setProperty", tpFunc, parser, key, value);
        }
      });
		  } catch (Exception e) {
		    throw new ParserException(e);
		  }
		}
		
		return key + " = " + value;
	}
	
	public void deleteSetting(Parser parser, String key, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		if (tokenName == null) { tokenName = LIB_JADEBRINGER_SETTINGS; }
    
    TokenWrapper token = FunctionCaller.findToken(tokenName, null, true);
    try {
    FunctionCaller.doWithToken(parser, token, new Run<Object>() {
      public Object run() throws Exception {
        return FunctionCaller.callFunction("resetProperty", tpFunc, parser, key);
      }
    });
    } catch (Exception e) {
      throw new ParserException(e);
    }
	}
	
	public Object getSetting(Parser parser, String key, Object defaultValue, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		if (tokenName == null) { tokenName = LIB_JADEBRINGER_SETTINGS; }
		
		Object result;
    if (tokenName.toLowerCase().startsWith("lib:")) {
      result = FunctionCaller.callFunction("getLibProperty", tpFunc, parser, key, tokenName);
    } else {
      TokenWrapper token = FunctionCaller.findToken(tokenName, null, true);
      try {
      result = FunctionCaller.doWithToken(parser, token, new Run<Object>() {
        public Object run() throws Exception {
          return FunctionCaller.callFunction("getProperty", tpFunc, parser, key);
        }
      });
      } catch (Exception e) {
        throw new ParserException(e);
      }
    }
		
		if ("".equals(result) && defaultValue != null) {
			result = defaultValue;
		}
		
		return result;
	}
	
	public Object listSettings(Parser parser, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		if (tokenName == null) { tokenName = LIB_JADEBRINGER_SETTINGS; }
    
		String names = (String)FunctionCaller.callFunction("getMatchingLibProperties", tpFunc, parser, ".*", tokenName);

		Token token = MapTool.getParser().getTokenMacroLib(tokenName);
		//JSONObject result = new JSONObject();
		StringBuilder sb = new StringBuilder();
		for(String key : names.split(",")) {
			//Object value = getSetting(parser, key, null, tokenName);
			Object value = token.getProperty(key);		
			//result.put(key, value);
			sb.append(key).append(" = ").append(value).append("<br>");
		}
				
		return sb.toString();
	}
}
