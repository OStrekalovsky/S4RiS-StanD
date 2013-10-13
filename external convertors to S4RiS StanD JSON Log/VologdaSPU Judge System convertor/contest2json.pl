#!perl -w

require "constants.pl";
require "contest_info.pl";
require "users_passwords.pl";

$contestName = $VologdaSPUtrainings;

open F1, ">contest.json" or die;
print F1 "{\n";
print F1 "    \"contestName\": \"$contestName\",\n";
print F1 "    \"freezeTimeMinutesFromStart\": $freeze_start_min,\n";
print F1 "    \"problemLetters\":\n";
print F1 "    [\n";
$first = 1;
foreach $problem_letter (@problem_letters) {
    if ($first) {
        $first = 0;
    } else {
        print F1 ",\n";
    }
    print F1 "        \"$problem_letter\"";
}
print F1 "\n";
print F1 "    ],\n";
print F1 "    \"contestants\":\n";
print F1 "    [\n";
$first = 1;
foreach $user_name (sort keys %password) {
    if ($first) {
        $first = 0;
    } else {
        print F1 ",\n";
    }
    print F1 "        \"$user_name\"";
}
print F1 "\n";
print F1 "    ],\n";
print F1 "    \"runs\":\n";
print F1 "    [\n";
open F2, "<checked.txt";
@lines = <F2>;
close F2;
chomp @lines;

$first = 1;
foreach $line (@lines) {
    my ($source_id, $minute, $compiler, $test_number, $check_result, $problem_id, $user_name) = split /\|/, $line;
    my $problem_letter = $problem_letter_by_id{$problem_id};
    my $success;
    if ($check_result eq $accepted) {
        $success = "true";
    } else {
        $success = "false";
    }
    if ($first) {
        $first = 0;
    } else {
        print F1 ",\n";
    }
    print F1 "        {\n";
    print F1 "            \"contestant\": \"$user_name\",\n";
    print F1 "            \"problemLetter\": \"$problem_letter\",\n";
    print F1 "            \"timeMinutesFromStart\": $minute,\n";
    print F1 "            \"success\": $success\n";
    print F1 "        }";
}
print F1 "\n";
print F1 "    ]\n";
print F1 "}\n";
close F1;