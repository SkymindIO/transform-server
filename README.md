# TransformServer

HTTP server for python transform execution.


## Starting server:

```
./server.sh
```

## API

### `add`

Adds a transform.

##### Parameters:

`name`: Name of the transform.
`code`: Python code
`input`: Dictionary mapping from input variable names to types. (Available types are `str`, `int`, `float` and `ndarray`. More types coming soon.)
`output`: Dictionary mapping from output variable names to types.

##### Example:

```
localhost:8000/add?
name=my transform
&code=z = x + y
&input={'x': 'str', 'y': 'str'}
&output={'z': 'str'}
```

### `exec` 

Exceutes a transform

##### Parameters:

`name`: Name of the transform to execute.
`input`: Dictionary mapping from input variables names to values.

##### Example:

```
localhost:8000/exec?
name=my transform
&input={'x': 'Hello ', 'y': 'World!'}
```

