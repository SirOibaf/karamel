name              'test'
maintainer        'The Authors'
maintainer_email  'you@example.com'
license           'All Rights Reserved'
description       'Installs/Configures test-chef'
long_description  'Installs/Configures test-chef'
version           '0.1.0'
chef_version      '>= 13.0'

support 'ubuntu'
support 'centos'

recipe 'test::install', 'Install the test recipe'
recipe 'test::default', 'Default recipe'

attribute "test/attribute",
          :description => "attribute1",
          :type => "string"

attribute "test/nested/attribute",
          :description => "nested attribute",
          :type => "string"

attribute "test/default_attribute",
          :description => "nested attribute",
          :default => "default",
          :type => "string"

attribute "test/required_attribute",
          :description => "nested attribute",
          :required => "required",
          :type => "string"

attribute "test/numeric_attribute",
          :description => "numeric_attribute",
          :type => "numeric"

attribute "test/boolean_attribute",
          :description => "boolean attribute",
          :type => "boolean"

attribute "test/array_attribute",
          :description => "array attribute",
          :type => "array"

