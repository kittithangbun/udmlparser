package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Subject Area Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class SubjectArea implements UDMLObject {

	private String			subjectAreaID;
	private String			subjectAreaName;
	private Vector <String>	logicalTablesIDs = null;
	private Vector <String> hierarchyDimensionIDs = null;

	private void parseLogicalTable(String line) {
		int iIndexSA = line.indexOf(") SUBJECT AREA ");
		if (line.endsWith(",")) {
			logicalTablesIDs.add(line.substring(0, line.length()-1));
		}
		if (iIndexSA != -1) {
			logicalTablesIDs.add(line.substring(0, iIndexSA));
		}
	}

	private void parseHierDim(String line) {
		hierarchyDimensionIDs.add(line.substring(0, line.length()-1));
	}

	public SubjectArea (String declare, String sSubjectArea, BufferedReader udml) {
		String line;
		String trimmedHeader = declare.trim();
		int asIdx = trimmedHeader.indexOf(" AS ");
		int iconIds = trimmedHeader.indexOf(" ICON INDEX ");
		subjectAreaID = trimmedHeader.substring(sSubjectArea.length(),asIdx).trim().replaceAll("\"", "");

		if (iconIds != -1) {
			subjectAreaName = trimmedHeader.substring(asIdx+4, iconIds).trim().replaceAll("\"", "");
		}
		else {
			subjectAreaName = trimmedHeader.substring(asIdx+4).trim().replaceAll("\"", "");
		}

		try {
			line = udml.readLine();

			//HIERARCHY DIMENSIONS
			if (line.endsWith("DIMENSIONS (")) {
				hierarchyDimensionIDs = new Vector<String>();
				line = udml.readLine().trim().replaceAll("\"", "");
				while (( line.indexOf("LOGICAL TABLES (") == -1) || 
						(line.indexOf("PRIVILEGES") != -1 && 
						line.indexOf(";") != -1)) {
					parseHierDim(line);
					line = udml.readLine().trim().replaceAll("\"", "");
				};
			}

			//LOGICAL TABLES LIST
			if (line.endsWith("LOGICAL TABLES (")) {
				logicalTablesIDs = new Vector<String>();
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
					parseLogicalTable(line);
				} while (line.indexOf(") SUBJECT AREA ") == -1);
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = udml.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}

	/**
	 * Subject Area XML serializer
	 * @param doc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document doc) {
		if (subjectAreaID == null) {
			subjectAreaID = "";
		}
		Node nBusinessCatalogID = doc.createTextNode(subjectAreaID);
		if (subjectAreaName == null) {
			subjectAreaName = "";
		}
		Node nBusinessCatalogName = doc.createTextNode(subjectAreaName);

		Element eBusinessCatalog = doc.createElement("BusinessCatalog");
		Element eBusinessCatalogID = doc.createElement("BusinessCatalogID");
		Element eBusinessCatalogName = doc.createElement("BusinessCatalogName");

		eBusinessCatalogID.appendChild(nBusinessCatalogID);
		eBusinessCatalogName.appendChild(nBusinessCatalogName);

		eBusinessCatalog.appendChild(eBusinessCatalogID);
		eBusinessCatalog.appendChild(eBusinessCatalogName);

		Element eHierDimensionList = doc.createElement("HierarchyDimensionIDList");
		Element eHierDim = null;
		Node nHierDim = null;

		if (hierarchyDimensionIDs != null)
			for (String sHierDimID : hierarchyDimensionIDs) {
				eHierDim = doc.createElement("HierarchyDimensionID");
				if (sHierDimID == null) {
					sHierDimID = "";
				}
				nHierDim = doc.createTextNode(sHierDimID);

				eHierDim.appendChild(nHierDim);
				eHierDimensionList.appendChild(eHierDim);
			}

		eBusinessCatalog.appendChild(eHierDimensionList);

		Element eLogicalTableList = doc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (logicalTablesIDs != null)
			for (String logicalTableID : logicalTablesIDs) {
				eLogicalTable = doc.createElement("LogicalTableID");
				if (logicalTableID == null) {
					logicalTableID = "";
				}
				nLogicalTable = doc.createTextNode(logicalTableID);

				eLogicalTable.appendChild(nLogicalTable);
				eLogicalTableList.appendChild(eLogicalTable);
			}

		eBusinessCatalog.appendChild(eLogicalTableList);
		return eBusinessCatalog;
	}
}
/*
 * DECLARE SUBJECT AREA <FQ Subject Area Name> AS <Subject Area Name>
 * DIMENSIONS (
 * <hierarchy dim>,
 * <hierarchy dim>)
 * LOGICAL TABLES (
 * <FQ logical tbl>,
 * <FQ logical tbl>) SUBJECT AREA <status>
 * PRIVILEGES (<...>);
 */