var theme_list = "themes";
var theme_extension = ".xml";
var theme_dir = "themes/";

function theme_path( theme_filename ) {
	return theme_dir + theme_filename + theme_extension;
}

/*
   Theme validation
*/
function is_valid_theme( theme_filename ) {
	// if valid, there exists at least one theme tag
	return loadXML( theme_filename ).getElementsByTagName("theme").length > 0
}

// make sure every entry in our collection (theme_filenames) is a valid theme (not a motif)
function validate_theme_library () {
	var result = true;
	for (var i=0; i < theme_filenames.length; i++ ) {
		//result = result && is_valid_theme( theme_dir + theme_filenames[i] + theme_extension );
		if (! is_valid_theme( theme_path( theme_filenames[i] ) ) )
			return theme_filenames[i];
	}
	return result;
}

/*
	Theme loading
*/

function theme_correct_spelling( spelling ) {
	switch( spelling ) {
		case 'fireworksnight':
			return 'fireworks night';
		default:
			return spelling;
	}
}

function load_theme( theme_filename, theme_name ) {
	var xml_DOM = loadXML( theme_filename );
	var root_element = xml_DOM.getElementsByTagName("theme")[0] // assume only one element
	//theme_data[theme_data.length] = root_element;
	var corrected_theme_name = theme_correct_spelling( theme_name.toLowerCase() ).toLowerCase();
	theme_data[ corrected_theme_name ] = process_theme( root_element, corrected_theme_name );
}

function process_theme( xml, name ) {

	var motif_nodes = xml.getElementsByTagName( "motif-component" );
	var theme_nodes = xml.getElementsByTagName("theme-component");
	
	var obj = [];
	
	obj.name = name;
	obj.motifs = [];
	obj.subthemes = [];
	
	// iterate through motif_nodes and put into obj.motifs as strings
	for ( var i=0; i<motif_nodes.length; i++ ) {		
		var motif_node = motif_nodes[i].childNodes[0]; // assume only one component in a feature
		if ( ! undefined_check( motif_node ) ) {
			var motif_str = motif_node.nodeValue.replace(" ", "");
			var corrected_motif = correct_spelling( motif_str.toLowerCase() ).toLowerCase();
			obj.motifs.push( corrected_motif ); 
			
			//disp ( 'checking required motifs ['+ corrected_motif + ']');
			//disp ( motif_data[ corrected_motif ] )
			//disp ( undefined_check( motif_data[ corrected_motif ] ) );
			
			//assume motif loading already done
			if ( undefined_check( motif_data[ corrected_motif ] ) ) {
				load_motif(motif_path(motif_str), motif_str);
				disp('loaded motif '+corrected_motif);

				//var motif_obj = make_motif_obj( corrected_motif );
				//if (! undefined_check( motif_obj ) ) {
				//	motif_data[ corrected_motif ] = motif_obj;
				//}

				//alert( 'created '+ corrected_motif + ' motif required by ' + name );
			}
		} else {
			//disp( motif_node );
			alert( 'undefined! ');
		}
	}
	
	// iterate through theme_nodes and put into obj.subthemes as strings
	// do we need to fix names with spaces here?
	for ( var j=0; j<theme_nodes.length; j++ ) {		
		var theme_node = theme_nodes[j].childNodes[0]; // assume only one component in a feature
		if ( ! undefined_check( theme_node ) ) {
			var theme_str = theme_node.nodeValue;
			obj.subthemes.push( theme_correct_spelling( theme_str.toLowerCase() ).toLowerCase() );
		}
	}
	
	//alert(' motif count in theme '+name+ ' ' + obj_size (obj.motifs) );
	obj.name = name;
	return obj;
}

function load_all_themes () {
	var xml = loadXML( theme_path( theme_list ) );
	var root_element = xml.getElementsByTagName("themes")[0] // assume only one element
	var theme_nodes = root_element.getElementsByTagName("theme");

	for ( var j=0; j<theme_nodes.length; j++ ) {
		var theme_node = theme_nodes[j].textContent;

		load_theme( theme_path( theme_node ), theme_node );
	}
}