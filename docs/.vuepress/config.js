module.exports = {
  title: 'Streamy',
  description: 'Build powerful reactive, concurrent, and distributed applications stream applications.',
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'About', link: '/about/' },
      { text: 'Guide', link: '/guide/' },
      { text: 'Develop', link: '/develop/' },
    ],
    sidebar: {
      '/about/': [
        {
          title: 'About',
          collapsable: false,
          children: [
            '',
            'concept'
          ]
        }
      ],
      '/guide/': [
        {
          title: 'Guide',
          collapsable: false,
          children: [
            '',
            'setup',
            'administration'
          ]
        },
      ],
      '/develop/': [
        {
          title: 'Develop',
          collapsable: false,
          children: [
            '',
            'create-a-plugin',
            'use-a-plugin',
            'recipe',
            'integration'
          ]
        }
      ]
    }
  }
}
