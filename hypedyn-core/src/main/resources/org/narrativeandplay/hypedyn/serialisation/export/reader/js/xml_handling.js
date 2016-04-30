/*
	Handle opening of xml file and conversion of xml format to a javascript object
*/

// xml_name is the xml filename
function loadXML( xml_name ) {
	if (window.XMLHttpRequest)
	  {
	  xhttp=new XMLHttpRequest();
	  }
	else // IE 5/6
	  {
	  xhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	  
	xhttp.open("GET", xml_name,false); // should really be async - alex
	 
	xhttp.send();
	//disp(xhttp.responseXML);
	return xhttp.responseXML;
}

// order matters, themes need motifs loaded first
function load_all_xml() {
	//load_all_motifs(); // changed to load from themes
	load_all_themes();
	//load_photo_xml();
}

// recursively remove text nodes with only \n \t and \r in it
// these are the formatting characters that we should ignore
function remove_empty_nodes( node ) {

	// returns true if text only contains \n \t \r
	function helper( str ) {
		var letters = str.split('');
		for (var i=0; i<letters.length; i++) {
			if ( letters[i] != '\n' &&
				 letters[i] != '\r' &&
				 letters[i] != '\t' )
				return false; 
		}
		return true; // no other letters found
	}
	
	// node type is TEXT_NODE
	if ( node.nodeType == node.TEXT_NODE &&
		 helper( node.nodeValue ) )
	{
		node.parentNode.removeChild( node );
	}
	
	// go through the child Nodes and delete 'illegal' text nodes
	for ( var i=0; i<node.childNodes.length; i++ ) {
		remove_empty_nodes( node.childNodes[i] );
	}
}

// node operation

function node_child( node ) {
	return node.childNodes;
}

function node_attr( node ) {
	return node.attributes;
}

function node_name( node ) {
	return node.nodeName;
}

function node_type( node ) {
	return node.nodeType;
}

function node_value( node ) {
	return node.nodeValue;
}

// eg xml
//  <bookstore>
//  	<book w='something'> a book </book>
//  </bookstore>
//
// access child by its tag name 
//  eg   obj.bookstore will return the array of bookstore tags
//       obj.bookstore.book will return the array of book tags
// access text content 
//    obj.book[0].text
// access attributes by doing 
//    obj.book[0].attr[ 'w' ]

// [BUG] look at current natom format, it throws error when i dont cover with natom.
function create_xml_js( xml_filename ) {

	//disp('[xml filename] '+xml_filename);
	
	var xml_DOM = loadXML( xml_filename );	
	//disp('xml filename '+xml_filename);
	//disp(' xml_DOM in create_xml js '+xml_DOM);
	// remove empty space, tabs and return carriage
	remove_empty_nodes( xml_DOM );
	
	function helper( xml_DOM ) {
		
		var retval = [];
		retval._attr = [];
		
		for (var i=0; i<xml_DOM.childNodes.length; i++ ) {
			
			var tag_name = xml_DOM.childNodes[i].nodeName;
			//disp( 'tag name '+tag_name );
			
			if ( tag_name == '#text') {
				retval.text = xml_DOM.childNodes[i].nodeValue;
			}
			else  {
				
				if ( undefined_check( retval[tag_name] ) )
					retval[tag_name] = [];
			
				var child = xml_DOM.childNodes[i];
				//disp ( 'passing child ' + child );
				retval[tag_name].push( helper( child ) );
			}
		}
		
		if (! xml_DOM.attributes === null ) {
			for (var j=0; j<xml_DOM.attributes.length; j++) {
				var attr = xml_DOM.attributes[j];
				var attr_name = attr.nodeName;
				var attr_val = attr.nodeValue;
				retval._attr[attr_name] = attr_val;
			}
		}

		return retval;
	}
	
	return helper( xml_DOM );
}