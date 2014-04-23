package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import obiee.udmlparser.utils.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Logical Table Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalTable implements UDMLObject {

	private String			logicalTableID;
	private String			logicalTableName;
	private Vector <String>	logicalColumnIDs = null;
	private Vector <String>	logicalColumnNames = null;
	private Vector <String> logicalColumnDescriptions = null;
	private Vector <String>	derivedLogicalColumnExpressions = null;
	private Vector <String>	derivedColumnMappings = null;

	public LogicalTable (String declare,
			String logicalTable,
			BufferedReader udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");
		logicalTableID = trimmedDeclareStatement.substring( logicalTable.length(), iIndexAS).
				trim().replaceAll("\"", "");
		logicalTableName = trimmedDeclareStatement.substring( iIndexAS + 4, 
				trimmedDeclareStatement.indexOf(" HAVING")).
				trim().replaceAll("\"", "");

		try {
			line = udml.readLine();
			logicalColumnIDs = new Vector<String>();
			logicalColumnNames = new Vector<String>();
			logicalColumnDescriptions = new Vector<String>();
			derivedLogicalColumnExpressions = new Vector<String>();
			do {
				line = udml.readLine().trim();
				if (line.indexOf(" AS ") != -1) {
					//FQLOGCOLNAME
					logicalColumnIDs.add(line.substring(0, 
							line.indexOf(" AS ")).
							trim().replaceAll("\"", ""));
					//LOGCOLNAME
					if (line.indexOf(" {") != -1)
						logicalColumnNames.add(line.substring(
								line.indexOf(" AS ")+4,
								line.indexOf(" {")).
								trim().replaceAll("\"", ""));
					else
						logicalColumnNames.add(line.substring(
								line.indexOf(" AS ")+4).
								trim().replaceAll("\"", ""));
					if (line.indexOf(" DESCRIPTION") != -1)
						logicalColumnDescriptions.add(line.substring(
								line.indexOf(" {")+2,
								line.indexOf("} ")).
								trim());
					else
						logicalColumnDescriptions.add("");
					//DERIVED EXPRESSION
					if (line.indexOf(" DERIVED") != -1)
						derivedLogicalColumnExpressions.add(line.substring(
								line.indexOf(" {")+2, 
								line.indexOf("} ")).
								trim());
					else
						derivedLogicalColumnExpressions.add("");
				}
			} while (line.indexOf("KEYS (") == -1 &&
					line.indexOf("SOURCES (") == -1);

			//DISCARD SOURCES, DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = udml.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		trimmedDeclareStatement	= null;
		line = null;
	}

	/**
	 * Folder Attribute XML serializer
	 * @param doc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document doc) {
		if (logicalTableID == null) {
			logicalTableID = "";
		}
		Node nLogicalTableID = doc.createTextNode(logicalTableID);
		if (logicalTableName == null) {
			logicalTableName = "";
		}
		Node nLogicalTableName = doc.createTextNode(logicalTableName);

		Element eLogicalTable = doc.createElement("LogicalTable");
		Element eLogicalTableID = doc.createElement("LogicalTableID");
		Element eLogicalTableName = doc.createElement("LogicalTableName");

		eLogicalTableID.appendChild(nLogicalTableID);
		eLogicalTableName.appendChild(nLogicalTableName);

		eLogicalTable.appendChild(eLogicalTableID);
		eLogicalTable.appendChild(eLogicalTableName);

		Element eLogicalColumnList = doc.createElement("LogicalColumnList");
		Element eLogicalColumn = null;
		Element eLogicalColumnID = null;
		Element eLogicalColumnName = null;
		Element eLogicalColumnDescription = null;
		Element eLogicalColumnDerivedExpression = null;

		Element eLogicalColumnDerivedMappingList = null;
		Element eBiz2BizColumnMappingID = null;
		Node nBiz2BizColumnMappingID = null;

		Node nLogicalColumnID = null;
		Node nLogicalColumnName = null;
		Node nLogicalColumnDescription = null;
		Node nLogicalColumnDerivedExpression = null;

		if(logicalColumnIDs != null)
			for (int i=0; i< logicalColumnIDs.size(); i++) {
				eLogicalColumn = doc.createElement("LogicalColumn");
				eLogicalColumnID = doc.createElement("LogicalColumnID");
				eLogicalColumnName = doc.createElement("LogicalColumnName");
				eLogicalColumnDescription = doc.createElement("LogicalColumnDescription");
				eLogicalColumnDerivedExpression = doc.createElement("LogicalColumnDerivedExpression");

				if (logicalColumnIDs.get(i) == null) {
					nLogicalColumnID = doc.createTextNode("");
				} else {
					nLogicalColumnID = doc.createTextNode(logicalColumnIDs.get(i));
				}

				if (logicalColumnNames.get(i) == null) {
					nLogicalColumnName = doc.createTextNode("");
				} else {
					nLogicalColumnName = doc.createTextNode(logicalColumnNames.get(i));
				}

				nLogicalColumnDescription = doc.createTextNode((logicalColumnDescriptions.get(i)).replace("\"", ""));

				if ((derivedLogicalColumnExpressions.get(i)).replaceAll("\"", "") == null) {
					nLogicalColumnDerivedExpression = doc.createTextNode("");
				} else {
					nLogicalColumnDerivedExpression = doc.createTextNode((derivedLogicalColumnExpressions.get(i)).replaceAll("\"", ""));
				}

				eLogicalColumnID.appendChild(nLogicalColumnID);
				eLogicalColumnName.appendChild(nLogicalColumnName);
				eLogicalColumnDescription.appendChild(nLogicalColumnDescription);
				eLogicalColumnDerivedExpression.appendChild(nLogicalColumnDerivedExpression);

				eLogicalColumn.appendChild(eLogicalColumnID);
				eLogicalColumn.appendChild(eLogicalColumnName);
				eLogicalColumn.appendChild(eLogicalColumnDescription);
				eLogicalColumn.appendChild(eLogicalColumnDerivedExpression);

				eLogicalColumnDerivedMappingList = doc.createElement("LogicalColumnDerivedMappingList");
				derivedColumnMappings = Utils.CalculationParser(logicalTableID, derivedLogicalColumnExpressions.get(i), true);
				if(derivedColumnMappings != null) {
					for (String biz2BizColumnMapping : derivedColumnMappings) {
						eBiz2BizColumnMappingID = doc.createElement("LogicalColumnDerivedMappingID");
						if (biz2BizColumnMapping == null) {
							biz2BizColumnMapping = "";
						}
						nBiz2BizColumnMappingID = doc.createTextNode(biz2BizColumnMapping);

						eBiz2BizColumnMappingID.appendChild(nBiz2BizColumnMappingID);
						eLogicalColumnDerivedMappingList.appendChild(eBiz2BizColumnMappingID);
					}
					eLogicalColumn.appendChild(eLogicalColumnDerivedMappingList);
				}
				eLogicalColumn.appendChild(eLogicalColumnDerivedMappingList);

				eLogicalColumnList.appendChild(eLogicalColumn);
			}

		eLogicalTable.appendChild(eLogicalColumnList);
		return eLogicalTable;
	}
}
/*
 * DECLARE LOGICAL TABLE <FQ logical tbl name> AS <logical tbl name> HAVING
 * (
 * <FQ logical column name> AS <logical column (fantasy) name>
 * PRIVILEGES ( READ),
 * <FQ logical column name> AS <logical column (fantasy) name>
 * PRIVILEGES ( READ) )
 * KEYS (
 * <FQ key column (logical column)> )
 * SOURCES (
 * <FQ logical table source> ) DIAGRAM POSITION (<int>, <int>)
 * PRIVILEGES ( READ);
 */
