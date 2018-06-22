package ohkk.test.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SetAllMembers {

	private final String STR_UNDERSCORE="_";
	/**
	 * リストの子要素の名称取得
	 * 
	 * @param name 要素名
	 * @param parentName 親の要素名
	 * @param index 付加するインデックス番号
	 * @return 要素名（[parentName].[name][index]）
	 */
	private String getParamName(String name, String parentName, Integer index) {
		StringBuffer sb = new StringBuffer();
		if (parentName != null) {
			sb.append(parentName);
		}
		if (index != null) {
			sb.append(index);
			sb.append(STR_UNDERSCORE);
		}
		sb.append(name);
		return sb.toString();
	}
	
	/**
	 * 検索条件を設定する。
	 * 文字列・文字列リストは自動でセットし、複雑は条件はSQLに合わせて成型してセットする。
	 * ※リスト型で要素が文字列ではない場合、「[リスト名].[内部要素名][連番]」で設定する
	 * 
	 * @param params リクエストDTO
	 * @param targetClass 設定するタイプセーフパラメータ
	 * @param parentName 親名
	 * @param idx インデックス番号
	 * @return 設定済みタイプセーフパラメータ
	 */
	private Map<String, Object> getSqlParams(
			Map<String, Object> params, Object targetClass, String parentName, Integer idx) {
		System.out.println("--------------- 変換後パラメータ ---------------");
		// 検索条件を設定する
		try {
			for (Field field : targetClass.getClass().getFields()) {
				field.setAccessible(true);
				Object obj = field.get(targetClass);
				// privateメンバーは処理しない
				if (Modifier.isPrivate(field.getModifiers())) {
					continue;
				}
				// 型に合わせてパラメータにセットする
				if (field.getType() == String.class) {
					String value = (String) field.get(targetClass);
					if (StringUtils.isEmpty(value)) {
						value = null;
					}
					params.put(getParamName(field.getName(), parentName, idx), value);
					System.out.println(getParamName(field.getName(), parentName, idx)+":"+value+":");
				} else if (field.getType() == List.class) {
					List innerList = (List) field.get(targetClass);
					if (innerList == null || innerList.isEmpty()) {
						//null または　サイズ０の場合はnullを設定する
						params.put(getParamName(field.getName(), parentName, idx), null);
						System.out.println(getParamName(field.getName(), parentName, idx)+":null");
					} else {
						// 要素が文字列の場合はそのままセットする
						if (innerList.get(0) instanceof String) {
							params.put(getParamName(field.getName(), parentName, idx), (List) field.get(targetClass));
							System.out.println(getParamName(field.getName(), parentName, idx) + ":"
									+ (List) field.get(targetClass) + ":");
						} else {
							for (int i = 0; i < innerList.size(); i++) {
								String parentAddedName = field.getName();
								if (parentName != null) {
									parentAddedName = getParamName(field.getName(), parentName, idx);
								}
								getSqlParams(params, innerList.get(i), parentAddedName, new Integer(i + 1));
							}
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			System.out.println("errors.request.transform");
		}
		System.out.println("--------------- 変換後パラメータ ---------------");
		return params;
	}

	
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

}
