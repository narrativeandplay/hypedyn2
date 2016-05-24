// thematic recommendation algorithm, adapted from Charlie Hargood's Thematic Model Builder

var motif_data = [];
var theme_data = [];

// down case and remove trailing spaces, and strip out newlines
function process_natom( natom_str ) {
	natom_str = natom_str.toLowerCase();
	natom_str = natom_str.replace(/\r?\n|\r/g, " ");
	return trim( natom_str );
}

// motifs is an array of motif names (string)
// natom is just a string (a tag)
function motifs_covered ( motifs, natom ) {

	var features = [];
	
	// remove spaces at head and tail of string and down case natom
	natom = process_natom(natom);
	
	//debug code
	var debug_str = "";
	for( var i in motifs ) {
		debug_str += (' [debug2] '+ motifs[i] +"\r\n");
		motif_obj = motif_data[ motifs[i] ];
		features = features.concat( motif_obj.features );
		
		//debug features
		//for( var i in motif_obj.features ) {
		//	debug_str += ( '  [debug3] i ' +i +" "+motif_obj.features[i]+"\r\n");
		//}
	}
	
	// test whether any of the features can be found in the natom 
	for ( var k=0; k<features.length; k++ ) {
		//disp( '[motifs_covered] feature '+ features[k] );
		//disp( '[motifs_covered] natom '+natom );
		if( natom != ''  && 
			//( natom == features[k] )
			str_contains_phrase( natom, features[k] )
			) {
			//
			//str_contains( features[k], natom )
			//disp( debug_str );
			
			//disp( 'natom matched '+natom + ', feature: ' + features[k]);
			//disp( "[motif_covered]aa!!  " + features[k] + ' matched ' + natom+"\r\n");
			return true;
		}
	}
	return false;
}

// themes input is an array of theme names (string)
// returns theme hit ratio 
// returns a 0 is natoms undefined
// theme_dup if true will keep count of how many times each themes hit
//           if false it will always count 1 if hit and undefined if not hit (not pushed into the array)
//           false when not given ( undefined )
function theme_coverage ( themes, natom, theme_dup ) {

	//disp(' in theme_coverage, checking against '+ obj_size(themes) + ' themes...' );
	var theme_hit = 0;
	var theme_hits = [];
	
	for( i in themes ) { // i in number index

		var theme_name = themes[i].name;
		
		//disp( '[theme_coverage] theme name: '+theme_name);
		var theme_obj = theme_data[ theme_name ];

		if (! undefined_check( theme_obj )) {
			//disp('theme obj: '+ theme_obj);
			//disp('theme obj motifs: '+ theme_obj.motifs);
			//disp( 'natom text: '+natom +' trying to match against theme: '+theme_name);

			// need to do this recursively
			function get_theme_motifs(theme_obj){
				var combined_motifs = [];

				// add this theme's motifs
				for ( i in theme_obj.motifs ) {
					combined_motifs.push(theme_obj.motifs[i]);
				}

				// and add all the subthemes' motifs
				for ( i in theme_obj.subthemes ) {
					var this_subtheme_name = theme_obj.subthemes[i];
					var this_subtheme = theme_data[ this_subtheme_name ];
					if (! undefined_check(this_subtheme)){
						var this_subtheme_combined_motifs = get_theme_motifs(this_subtheme, combined_motifs);
						if(! undefined_check(this_subtheme_combined_motifs)){
							combined_motifs=combined_motifs.concat(this_subtheme_combined_motifs);
						}
					}
				}

				return combined_motifs;
			}

			// match motifs in theme and all subthemes in the theme
			var combined_motifs = get_theme_motifs(theme_obj);
			//disp( 'combined_motifs: '+ combined_motifs);

			if ( motifs_covered( combined_motifs, natom ) )
				//|| natoms[j] == theme_name ) // testing improvements to theme name match
				//|| str_contains_phrase( natom, theme_name ) )
			{
				function theme_exists( arr, name ) {
					for( k in arr ) {
						if ( arr[k].name == name )
							return true;
					}
					return false;
				}

				function get_theme( arr, name ) {
					for ( x in arr ) {
						if ( arr[x].name == name )
							return arr[x]
					}
				}

				// if not already hit, put it in
				//if ( undefined_check( theme_hits[ theme_obj.name ] ) ) {
				if ( ! theme_exists( theme_hits, theme_obj.name ) ) {
					var obj = [];
					obj.hit_count = 1;
					obj.name = theme_obj.name;

					theme_hits.push(obj);
				} else if ( theme_dup ) {
					var theme_hit_obj = get_theme( theme_hits, name );
					// need to increment if theme_dup true
					theme_hit_obj.hit_count += 1;
				}

				theme_hit++;
				//break; // why was this here? - alex

				disp( 'matched: '+ theme_name);
			}
		}
	}
	var themes_len = obj_size( themes );
	
	disp( 'themes matched: '+theme_hit+ ' out of '+themes_len + ' themes');
	//disp( 'themes hit '+theme_hit );
	//alert ( 'themes_len '+ themes_len );
	
	var ret = [];
	if ( themes_len == 0 ) {
		//return 0;
		ret.score = 0;
		ret.hits = [];
	} else { 
		ret.score = theme_hit / themes_len;
		ret.hits = theme_hits;
	}
	return ret;
}

// themes input is an array of theme names (string)
// motif and subtheme hits ratio
function component_coverage ( themes, natom ) {
	var comp_hits = 0;
	var motifs = [] ;
	var subthemes = [];
	
	var overall_motifs_count = 0;
	var overall_subthemes_count = 0;
	
	//disp('subtheme len b4 '+ obj_size( subthemes ) );
	
	// consolidate the motifs and subthemes across our collection of themes
	for ( i in themes ) {
		var theme_name = themes[i].name;

		//disp( '[component_coverage] theme name: '+theme_name);
		var theme_obj = theme_data[ theme_name ];
		
		//disp( 'i '+i);
		//disp( 'curr them '+themes[i] );
		//disp( 'theme-obj '+theme_obj );
		
		motifs= motifs.concat(theme_obj.motifs);
		subthemes=subthemes.concat(theme_obj.subthemes);
		
		overall_motifs_count += obj_size( theme_obj.motifs );
		overall_subthemes_count += obj_size( theme_obj.subthemes );

	}

	// check each motif separately
	for( var i in motifs ) {
		var motif_covered = false;
		var motif_obj = motif_data[ motifs[i] ];
		var features = motif_obj.features;

		// test whether any of the features can be found in the natom
		for ( var k=0; k<features.length; k++ ) {
			if( natom != ''  &&
				str_contains_phrase( natom, features[k] )) {
				motif_covered = true;
			}
		}

		if(motif_covered) {
			comp_hits++;
		}
	}
	disp( 'component coverage (just motifs): '+comp_hits);

	// need to get the actual subthemes, not just the list of names
	var combined_subthemes = [];
	for ( i in subthemes ) {
		var this_subtheme_name = subthemes[i];
		var this_subtheme = theme_data[ this_subtheme_name ];
		if (! undefined_check(this_subtheme)){
			combined_subthemes.push(this_subtheme);
		}
	}

	var subtheme_coverage = theme_coverage( combined_subthemes, natom, false );
	comp_hits +=  subtheme_coverage.score * overall_subthemes_count;
	disp( 'component coverage (including subthemes): '+comp_hits);

	return comp_hits / ( overall_motifs_count + overall_subthemes_count );
}

/*
	formula
	TC = (t/T) * 100     //  Thematic coverage
	CC = (c/C) * 100     //  component coverage
	TQ = (TC + CC) / 2;  // averaging both terms
	
	C is the number of elements that connotes a theme ( total motif and subthemes in the theme )
	c is the number of components it hit (motif and subthemes)
	T is the total number of themes in our database
	t is the number of themes it hit
*/


// thematic link: get a recommendation of a node based on link text
// parameters:
// natom_text - text containing features
// in_nodelist - list of candidate nodes to recommend
// threshold - minimum score to recommend
// returns: a list of recommended nodes
function recommend(natom_text, in_nodelist, threshold) {
	disp('**************************** recommend start ******************************')

	disp('**************************** finding themes in fragment ******************************')
	disp('fragment: ' + natom_text)

	// get a list of themes that appear in the given text fragment;
	// result contains the overall score and a list of themes that matched
	var result = theme_coverage( theme_names(), natom_text, false); //theme_dup );
	disp( 'number of theme hits: '+result.hits.length);
	disp( 'overall score score: '+result.score );

	// extract the list of matched themes
	var theme_hits = [];
	for( var g in result.hits ) {
		var obj = [];
		disp( 'result.hits [' + g + '].name: ' + result.hits[g].name );
		disp( 'result.hits [' + g + '].hit_count: ' + result.hits[g].hit_count );
		obj.name = result.hits[g].name;
		theme_hits [theme_hits.length] = obj;
	}

	// now find the nodes that contain these themes (excluding current node)
	disp('**************************** finding nodes that match the themes ******************************')
	var node_hits = []; // store list of matching nodes
	function comesBefore( node1, node2 ) {
		return node1.score < node2.score;
	}
	for ( i in in_nodelist ) {
		var thisnode = in_nodelist[i];
		disp('++++++++++++ checking node: ' + thisnode.name)
		if(thisnode.id != currNodeID){
			// thematic coverage
			var this_result = theme_coverage( theme_hits, thisnode.content, false);
			disp('++++++++++++ score: ' + this_result.score);
			disp('++++++++++++ number of hits: ' + this_result.hits.length);

			// component coverage
			var comp_result = component_coverage(theme_hits, thisnode.content);
			disp('++++++++++++ component coverage: ' + comp_result);
			disp('combined score: ' + (this_result.score + comp_result)/2);

			if(this_result.hits.length > 0){
				var obj = [];
				obj.score = (this_result.score + comp_result)/2;
				obj.hits = this_result.hits;
				obj.id = thisnode.id;
				if(obj.score>=threshold) {
					insertSorted(node_hits, obj, comesBefore);
				}
			}
		}
	}

	disp('**************************** recommend_thematic_link end ******************************')

	return node_hits;
}
