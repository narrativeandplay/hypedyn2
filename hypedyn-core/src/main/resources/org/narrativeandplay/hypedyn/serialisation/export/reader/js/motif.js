var motif_dir = "themes/";

var motif_extension = ".xml";

// loaded the xml DOM into an array
var motif_data = [];

function motif_path( motif_filename ) {
	return motif_dir + motif_filename + motif_extension;
}

function is_valid_motif( motif_filename ) {
	var xml = loadXML( motif_filename );
	
	// if valid, there exists at least one motif tag
	if ( !undefined_check( xml ) )
		return xml.getElementsByTagName("motif").length > 0
	else 
		return false;
}

// make sure every entry in our collection (motif_filenames) is a valid motif (not a motif)
// [not called] for efficiency but can be called to whether the motif is loaded properly
function validate_motif_library () {
	var result = true;
	for (var i=0; i < motif_filenames.length; i++ ) {
		//result = result && is_valid_motif( motif_dir + motif_filenames[i] + motif_extension );
		if (! is_valid_motif( motif_path( motif_filenames[i] ) ) )
			return motif_filenames[i];
	}
	return result;
}

/*
	motif loading
*/

function correct_spelling( motif_name ) {
	switch( motif_name )
	{
		case 'Accident':
			return 'accident';
		case 'christmassymbol':
			return 'christmas symbol';
		case 'dangerousanimal':
			return 'dangerous animal';
		case 'dangerousenvironment':
			return 'dangerous environment';
		case 'eastersymbol':
			return 'easter symbol';
		case 'formaldress':
			return 'formal dress';
		case 'guyfawkes':
			return 'guy fawkes';
		case 'religioussymbol':
			return 'religious symbol';
		case 'soundsystem':
			return 'sound system';
		case 'thanksgivingsymbol':
			return 'thanksgiving symbol';
		case 'warmclothing':
			return 'warm clothing';
		default:
		  return motif_name;
	}
}

function load_motif( motif_filename, motif_name ) {

	var xml_DOM = loadXML( motif_filename );
	var root_element = xml_DOM.getElementsByTagName( "motif" )[0] // assume only one element
	
	var corrected_name = correct_spelling( motif_name.toLowerCase() ).toLowerCase();
	var motif_obj = process_motif_xml( root_element, corrected_name );

	motif_data[ corrected_name ] = motif_obj;
}

// create our own datastructure
function process_motif_xml( xml, name ) {
	//disp( 'process motifs '+ name );
	//var motif = get_motif( motif_name );
	//disp( motif );
	var feature_nodes = xml.getElementsByTagName("feature");
	
	// our own motif object datastructure
	var obj = [];
	obj.name = name;
	obj.features = [];
	
	for (var i=0; i<feature_nodes.length; i++ ) {
		var feature_node = feature_nodes[i].childNodes[0];
		if ( ! undefined_check( feature_node ) ) {
			var feature_str = feature_node.nodeValue;
			obj.features.push( feature_str ); // assume only one component in a feature
		}
	}
	obj.name = name;
	return obj;
}

// if motif with name does not exists but theme has such a component
// instantiate that motif with its own name as the only feature
function make_motif_obj( name ) {
	//disp(' make motif '+name);
	if ( ! undefined_check ( motif_data[ name ] ) ) {
		alert( 'WARNING! attempt to overwrite existing motif' );
		//disp ( name );
		return;
	}
	else {
		var obj = [];
		obj.features = [];
		obj.name = name;
		obj.features.push( name ) ; // one and only feature 
		return obj;
	}
}

function load_all_motifs () {
	for ( var i=0; i<motif_filenames.length; i++ ) {
		load_motif( motif_path( motif_filenames[i] ), motif_filenames[i] );
	}
}