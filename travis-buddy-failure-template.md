## Travis tests have failed
Hey @{{pullRequestAuthor}},
please read the following log in order to understand the failure reason. There might also be some helpful tips along the way.
It'll be awesome if you fix what's wrong and commit the changes.

{{#jobs}}
### {{displayName}}
{{#scripts}}
<details>
  <summary>
    <strong>
     Expand here
    </strong>
  </summary>

```
{{&contents}}
```
</details>
<br />
{{/scripts}}
{{/jobs}}
