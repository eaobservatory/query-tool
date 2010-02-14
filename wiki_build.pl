$cwd = `pwd` ;
chomp( $cwd ) ;

@packages = ( 'utils' , 'gui' , 'djava' , 'app' ) ;

$ot_root = '/Users/shart/Desktop/ot/install' ;

&clean ;
&compile( &classpath ) ;
&jar ;

sub clean
{
	foreach $package ( @packages )
	{
		`rm -rf $cwd/classes/edu/jach/qt/$package/*` ;
	}

	`rm -rf $cwd/lib/qt*.jar` ;
	print "Cleaned \n" ;
}

sub classpath
{
	@files = `find $ot_root/lib -name "*.jar"` ;

	@jar_files = () ;
	for $file ( @files )
	{
		chomp( $file ) ;
		push( @jar_files , $file ) ;
	}

	push( @jar_files , "$ot_root/tools/pal.jar" ) ;

	@files = `find $cwd/lib -name "*.jar"` ;

	for $file ( @files )
	{
		chomp( $file ) ;
		push( @jar_files , $file ) ;
	}

	push( @jar_files , "$cwd/drama/" ) ;

	$classpath = join( ':' , @jar_files ) ;

	return $classpath ;
}

sub compile
{
	$classpath = shift or die "No classpath argument \n" ;
	chdir( "$cwd/src/main/" ) or die "Could not cd to $cwd/src/main/ \n" ;
	foreach $package ( @packages )
	{
		@source_files = () ;
		@java_files = `find edu/jach/qt/$package -name "*.java"` ;
		foreach $source_file ( @java_files )
		{
			chomp( $source_file ) ;
			push( @source_files , $source_file ) ;
		}
		@compile_args = () ;
		push( @compile_args , '-target' , '1.6' ) ;
		push( @compile_args , '-d' , "$cwd/classes/" ) ;
		push( @compile_args , '-classpath' , $classpath ) ;
		push( @compile_args , '-sourcepath' , '.' ) ;
		push( @compile_args , @source_files ) ; 
		unless( system( 'javac' , @compile_args ) )
		{
		print "Compiled " . @source_files . " files\n" ;
		}
		else
		{
			die "Problems compiling\n" ;
		}
	}
	print "Compiled \n" ;
}

sub jar
{
	print "Jar'ing \n" ;
	chdir( "$cwd/classes" ) or die "Could not cd to $cwd/classes \n" ;
	`jar cf $cwd/lib/qt.jar .` ;
	chdir( "$cwd/src/main" ) or die "Could not cd to $cwd/classes \n" ;
	`jar cf $cwd/lib/qt-src.jar .` ;
}