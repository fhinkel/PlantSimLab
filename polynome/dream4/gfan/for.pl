for ($i=0;$i<1001;$i++) {
        $line = "echo \"J=$i;load \\\"func-for.m2\\\";J\"|M2";
        print "$line\J";
        system($line);
}